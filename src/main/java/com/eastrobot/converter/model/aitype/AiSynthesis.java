package com.eastrobot.converter.model.aitype;

import io.swagger.annotations.ApiModel;

/**
 * AI合成
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:42
 */
@ApiModel(description = "Ai合成结果封装", subTypes = {TTS.class})
public interface AiSynthesis extends AiType {
}
