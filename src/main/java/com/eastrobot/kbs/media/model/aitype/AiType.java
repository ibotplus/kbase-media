package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;

/**
 * Ai-Type
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:39
 */
@ApiModel(description = "AiType结果封装", subTypes = {AiSynthesis.class, AiRecognition.class})
public interface AiType {
}
