package com.eastrobot.kbs.media.model;

import com.alibaba.fastjson.JSON;
import com.eastrobot.kbs.media.model.aitype.AiType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * ResponseMessage
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-03 19:30
 */
@Getter
@Setter
@NoArgsConstructor
@ApiModel(description = "响应数据")
public class ResponseMessage<T extends AiType> implements Serializable {
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
    /**
     * 结果封装
     */
    @ApiModelProperty("结果封装")
    private T responseEntity;

    public ResponseMessage(String sn) {
        this.sn = sn;
    }

    public ResponseMessage(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    public ResponseMessage(ResultCode resultCode, String sn) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
        this.sn = sn;
    }

    public ResponseMessage(ResultCode resultCode, String sn, T responseEntity) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
        this.sn = sn;
        this.responseEntity = responseEntity;
    }

    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.message = resultCode.getMsg();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
