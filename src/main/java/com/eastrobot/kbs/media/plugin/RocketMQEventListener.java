package com.eastrobot.kbs.media.plugin;

import com.eastrobot.kbs.media.model.AiType;
import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.model.ResponseMessage;
import com.eastrobot.kbs.media.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;

/**
 * RocketMQEventListener
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-18 15:11
 */
@Slf4j
@Component
@ConditionalOnBean(AsyncMode.class)
public class RocketMQEventListener {

    /**
     * 异步上传的文件夹
     */
    @Value("${convert.async.output-folder}")
    private String ASYNC_OUTPUT_FOLDER;

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
    public void createFileEventListener(RocketMQCreateFileEvent event) throws Exception {
        MessageExt message = event.getMessage();
        String fileAbsolutePath = new String(message.getBody(), RemotingHelper.DEFAULT_CHARSET);
        log.info("receive event, start convert file {}", fileAbsolutePath);

        // 判断是否存在rs文件  存在跳过 不重复进行解析
        String rs = ASYNC_OUTPUT_FOLDER + FilenameUtils.getBaseName(fileAbsolutePath) + FileExtensionType.RS.pExt();
        if (new File(rs).exists()) {
            return;
        }

        HashMap<String, Object> recognitionParam = new HashMap<>();
        recognitionParam.put(Constants.IS_ASYNC_PARSE, true);
        recognitionParam.put(Constants.AI_RESOURCE_FILE_PATH, fileAbsolutePath);
        recognitionParam.put(Constants.AI_TYPE, AiType.GENERIC_RECOGNITION);

        ResponseMessage responseMessage = convertService.driver(recognitionParam);
        log.info("convert file complete : {}", fileAbsolutePath, responseMessage);
    }
}
