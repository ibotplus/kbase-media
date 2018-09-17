package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;

/**
 * AI识别
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:42
 */
@ApiModel(description = "Ai识别结果封装", subTypes = {ASR.class, OCR.class, VAC.class})
public interface AiRecognition extends AiType {
}
