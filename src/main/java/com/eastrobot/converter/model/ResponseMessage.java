package com.eastrobot.converter.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * ResponseMessage
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-03 19:30
 */
@Getter
@Setter
@ApiModel(description = "响应数据")
// @Data
public class ResponseMessage implements Serializable {
    @ApiModelProperty("错误码")
    private Integer errorCode;
    @ApiModelProperty("错误信息")
    private String errorMessage;

    public ResponseMessage(ErrorCode error) {
        this.errorCode = error.getCode();
        this.errorMessage = error.getMsg();
    }

    public ResponseMessage() {
        this.errorCode = ErrorCode.SUCCESS.getCode();
        this.errorMessage = ErrorCode.SUCCESS.getMsg();
    }
}
