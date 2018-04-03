package com.eastrobot.converter.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * RestMessage
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-03 19:30
 */
@Getter
@Setter
@ApiModel(description = "返回响应数据")
public class RestMessage implements Serializable {
    @ApiModelProperty(value = "是否成功")
    private boolean success = true;
    @ApiModelProperty(value = "返回对象")
    private Object data;
    @ApiModelProperty(value = "错误编号")
    private Integer errCode;
    @ApiModelProperty(value = "错误信息")
    private String message;
}
