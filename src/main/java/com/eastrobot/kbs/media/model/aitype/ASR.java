package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ASR
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:47
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "ASR结果封装")
public class ASR implements AiRecognition {
    /**
     * 音频内容
     */
    @ApiModelProperty("音频内容")
    private String audioContent;

    /**
     * 音频关键字
     */
    @ApiModelProperty("音频关键字")
    private String audioKeyword;
}
