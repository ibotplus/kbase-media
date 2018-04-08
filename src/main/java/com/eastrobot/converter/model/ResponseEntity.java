package com.eastrobot.converter.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * ResponseEntity
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-08 10:40
 */
@Getter
@Setter
@ApiModel(description = "结果封装")
public class ResponseEntity {
    /**
     * 解析文件类型
     */
    @ApiModelProperty("解析文件类型")
    private String fileType;
    /**
     * 图片内容
     */
    @ApiModelProperty("图片内容")
    private String imageContent;
    /**
     * 音频内容
     */
    @ApiModelProperty("音频内容")
    private String audioContent;
    /**
     * 图片关键字
     */
    @ApiModelProperty("图片关键字")
    private String imageKeyword;
    /**
     * 音频关键字
     */
    @ApiModelProperty("音频关键字")
    private String audioKeyword;
}
