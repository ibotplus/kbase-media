package com.eastrobot.kbs.media.plugin;

import lombok.Getter;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEvent;

/**
 * RocketMQCreateFileEvent
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-18 14:53
 */
@Getter
@ConditionalOnBean(AsyncMode.class)
public class RocketMQCreateFileEvent extends ApplicationEvent {
    private String topic;
    private String tags;
    private MessageExt message;

    public RocketMQCreateFileEvent(MessageExt message) {
        super(message);
        this.message = message;
        this.topic = message.getTopic();
        this.tags = message.getTags();
    }
}
