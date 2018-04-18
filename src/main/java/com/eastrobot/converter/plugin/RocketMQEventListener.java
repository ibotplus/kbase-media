package com.eastrobot.converter.plugin;

import com.eastrobot.converter.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * RocketMQEventListener
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-18 15:11
 */
@Slf4j
@Component
public class RocketMQEventListener {

    @Autowired
    private ConvertService convertService;

    /**
     *
     * 监听到推送来的消息 进行实际业务处理
     *
     * @author Yogurt_lei
     * @date 2018-04-18 16:06
     */
    @EventListener(condition = "#event.topic=='MQ_CREATE_FILE_TOPIC' && #event.tags=='MQ_CREATE_FILE_TAG'")
    public void createFileEventListener(RocketMQCreateFileEvent event) {
        MessageExt message = event.getMessage();
        try {
            String fileAbsolutePath = new String(message.getBody(), RemotingHelper.DEFAULT_CHARSET);
            log.info("start convert file : {}", fileAbsolutePath);
            log.error("receive event, start convert file {}", fileAbsolutePath);
            // 正式使用时下面注释打开
            // ResponseMessage responseMessage = convertService.driver(fileAbsolutePath, true);
            // log.info("convert file complete : {}", fileAbsolutePath, JSON.toJSONString(responseMessage));
        } catch (UnsupportedEncodingException e) {
            // 业务发生错误 重试 发送回broker 等待下次消费
            log.warn("HANDLE-MSG occurred exception. do {} times retires.", message.getReconsumeTimes());
            RocketMQConsumer.sendMessageBack(message);
        }
    }
}
