package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * TTS
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:50
 */
@Getter
@Setter
@AllArgsConstructor
@ApiModel(description = "TTS结果封装")
public class TTS implements AiSynthesis {
    /**
     * 原始文本
     */
    @ApiModelProperty("原始文本")
    private String originText;
    /**
     * 文字转语音音频 base64结果
     */
    @ApiModelProperty("TTS结果, base64")
    private String textAudio;
}
