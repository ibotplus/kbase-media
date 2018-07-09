package com.eastrobot.converter.model.tts;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-09 17:47
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("百度TTS参数")
public class BaiduTTS implements TTSOption {
    /**
     * 语速，取值0-9，默认为5
     */
    @ApiModelProperty(value = "语速，取值0-9，默认为5", allowableValues = "range[0, 5]", example = "5")
    private int spd = 5;
    /**
     * 音调，取值0-9，默认为5
     */
    @ApiModelProperty(value = "音调，取值0-9，默认为5", allowableValues = "range[0, 9]", example = "5")
    private int pit = 5;
    /**
     * 音量，取值0-15，默认为5
     */
    @ApiModelProperty(value = "音量，取值0-15，默认为5", allowableValues = "range[0, 15]", example = "5")
    private int vol = 5;
    /**
     * 发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女
     */
    @ApiModelProperty(value = "发音人选择, 0为女声，1为男声，3为情感合成-度逍遥，4为情感合成-度丫丫，默认为普通女",
            allowableValues = "range[0, 4]",
            example = "0")
    private int per = 0;

}
