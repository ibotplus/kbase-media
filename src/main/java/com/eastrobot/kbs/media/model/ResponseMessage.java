package com.eastrobot.kbs.media.model;

import com.alibaba.fastjson.JSON;
import com.eastrobot.kbs.media.model.aitype.AiType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.io.Serializable;

/**
 * ResponseMessage
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-03 19:30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String md5;

    private com.eastrobot.kbs.media.model.AiType aiType;
    /**
     * 结果封装
     */
    @ApiModelProperty("结果封装")
    private T responseEntity;

    public ResponseMessage(ResultCode resultCode) {
        this.code = resultCode.code();
        this.message = resultCode.msg();
    }

    public ResponseMessage(ResultCode resultCode, String md5) {
        this.code = resultCode.code();
        this.message = resultCode.msg();
        this.md5 = md5;
    }

    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.code();
        this.message = resultCode.msg();
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
