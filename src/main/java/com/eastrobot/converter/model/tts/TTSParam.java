package com.eastrobot.converter.model.tts;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-09 19:19
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("TTS参数")
public class TTSParam<T extends TTSOption> {
    /**
     * 待转语音文本内容
     */
    @ApiModelProperty(value = "待转语音文本内容", required = true, example = "测试文本内容")
    private String text;

    @ApiModelProperty(value = "TTS参数")
    T option;
}
