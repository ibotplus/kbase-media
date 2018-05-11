package com.eastrobot.converter.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * ResponseMessageAsync
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-11 15:10
 */
@Getter
@Setter
@ApiModel(description = "响应数据")
public class ResponseMessageAsync implements Serializable {
    /**
     * 结果码
     */
    @ApiModelProperty("解析结果码")
    private Integer code;
    /**
     * 结果信息
     */
    @ApiModelProperty("解析结果信息")
    private String message;
    /**
     * 序列号
     */
    @ApiModelProperty("序列号")
    public String sn;

    public ResponseMessageAsync(ResultCode result, String sn) {
        this.code = result.getCode();
        this.message = result.getMsg();
        this.sn = sn;
    }

    public ResponseMessageAsync() {
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMsg();
    }

    public ResponseMessageAsync(ResultCode resultCode, String sn, String message) {
        this.code = ResultCode.SUCCESS.getCode();
        this.sn = sn;
        this.message = message;
    }

    public void setResultCode(ResultCode result) {
        this.code = result.getCode();
        this.message = result.getMsg();
    }
}
