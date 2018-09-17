package com.eastrobot.kbs.media.plugin;

import com.eastrobot.kbs.media.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

/**
 * RocketMQConsumer
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-17 11:40
 */
@Slf4j
@Component
@ConditionalOnBean(AsyncMode.class)
public class RocketMQConsumer {
    /**
     * 消费者的组名
     */
    @Value("${apache.rocketmq.consumer.push-consumer}")
    private String consumerGroup;

    /**
     * NameServer 地址
     */
    @Value("${apache.rocketmq.namesrvAddr}")
    private String namesrvAddr;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private DefaultMQPushConsumer consumer;

    /**
     * Initialized required param for RocketMQ PushConsumer
     *
     * @author Yogurt_lei
     * @date 2018-04-18 15:31
     */
    @PostConstruct
    public void init() {
        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);//Consumer第一次启动从队列头部开始消费
        consumer.setNamesrvAddr(namesrvAddr);
        consumer.setMaxReconsumeTimes(Constants.MQ_RETRY_CONSUME);
        try {
            consumer.subscribe(Constants.MQ_CREATE_FILE_TOPIC, Constants.MQ_CREATE_FILE_TAG);
            consumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
                for (MessageExt msg : msgs) {
                    try {
                        // publish event to business method
                        String messageBody = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        log.info("RECEIVE-MSG && publishEvent >>> msgId: {}, body: {}", msg.getMsgId(), messageBody);
                        eventPublisher.publishEvent(new RocketMQCreateFileEvent(msg));
                    } catch (Exception e) {
                        log.error("RECEIVE-MSG too many retries.[ >{} times ]", Constants.MQ_RETRY_CONSUME);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    // 延迟5秒再启动，主要是等待spring事件监听相关程序初始化完成，
                    // 否则，会出现对RocketMQ的消息进行消费后立即发布消息到达的事件，
                    // 然而此事件的监听程序还未初始化，从而造成消息的丢失
                    consumer.start();
                } catch (Exception e) {
                    log.error("MQPushConsumer init occurred exception.", e);
                }

                log.info("MQPushConsumer has been started. {}", consumer);
            }).start();
        } catch (Exception e) {
            log.error("MQPushConsumer init occurred exception", e);
        }
    }

    @PreDestroy
    public void destroy() {
        consumer.shutdown();
        log.info("MQPushConsumer has been destroyed. {}", consumer);
    }
}
