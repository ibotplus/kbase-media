package com.eastrobot.kbs.media.model.aitype;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OCR
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-07-10 19:47
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "OCR结果封装")
public class OCR implements AiRecognition {
    /**
     * 图片内容
     */
    @ApiModelProperty("图片内容")
    private String imageContent;

    /**
     * 图片关键字
     */
    @ApiModelProperty("图片关键字")
    private String imageKeyword;
}
