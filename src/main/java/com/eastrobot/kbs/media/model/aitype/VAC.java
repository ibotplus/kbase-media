package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * VAC
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:49
 */
@Getter
@Setter
@AllArgsConstructor
@ApiModel(description = "VAC结果封装")
public class VAC implements AiRecognition {
    /**
     * ocr内容
     */
    @ApiModelProperty("ocr内容")
    private OCR ocr;

    /**
     * asr内容
     */
    @ApiModelProperty("asr内容")
    private ASR asr;
}
