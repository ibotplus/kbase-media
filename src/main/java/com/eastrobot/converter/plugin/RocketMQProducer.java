package com.eastrobot.converter.plugin;

import com.eastrobot.converter.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * RocketMQProducer
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-17 11:39
 */
@Slf4j
@Component
@ConditionalOnBean(AsyncMode.class)
public class RocketMQProducer {
    /**
     * 生产者的组名
     */
    @Value("${apache.rocketmq.producer.producer-group}")
    private String producerGroup;

    /**
     * NameServer 地址
     */
    @Value("${apache.rocketmq.namesrvAddr}")
    private String namesrvAddr;

    private static DefaultMQProducer producer;

    /**
     * Initialized required param for RocketMQ Producer
     *
     * @author Yogurt_lei
     * @date 2018-04-18 15:31
     */
    @PostConstruct
    public void init() {
        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(namesrvAddr);
        // 设置重试次数
        producer.setRetryTimesWhenSendFailed(Constants.MQ_RETRY_PRODUCT);
        producer.setRetryTimesWhenSendAsyncFailed(Constants.MQ_RETRY_PRODUCT);
        try {
            producer.start();
            log.info("RocketMQProducer has been started. {}", producer);
        } catch (Exception e) {
            log.error("RocketMQProducer init occurred exception", e);
        }
    }

    @PreDestroy
    public void destroy() {
        producer.shutdown();
        log.info("RocketMQProducer has been destroyed. {}", producer);
    }

    /**
     * 发送消息 异步 内部有重试机制
     * @param topic topic
     * @param tag tag
     * @param msg 消息主体
     *
     * @author Yogurt_lei
     * @date 2018-04-18 11:00
     */
    public static void sendMessage(String topic, String tag, String msg) {
        try {
            StopWatch sw = new StopWatch();
            Message message = new Message(topic, tag, msg.getBytes(RemotingHelper.DEFAULT_CHARSET));
            sw.start();
            SendResult result = producer.send(message);
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("SEND-MSG >>> msgId: {}, msg: {}, status: {}", result.getMsgId(), msg, result.getSendStatus());
                    log.info(sw.prettyPrint());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("SEND-MSG occurred exception.", e);
                }
            });
            sw.stop();

        } catch (Exception e) {
            log.error("SEND-MSG occurred exception.", e);
        }
    }
}
