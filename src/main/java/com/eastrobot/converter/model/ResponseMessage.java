package com.eastrobot.converter.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
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
@ApiModel(description = "响应数据")
public class ResponseMessage implements Serializable {
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
    private ResponseEntity responseEntity;

    public ResponseMessage(ResultCode result) {
        this.code = result.getCode();
        this.message = result.getMsg();
    }

    public ResponseMessage() {
        this.code = ResultCode.SUCCESS.getCode();
        this.message = ResultCode.SUCCESS.getMsg();
    }

    public void setResultCode(ResultCode result) {
        this.code = result.getCode();
        this.message = result.getMsg();
    }

    /**
     * 更新entity
     */
    public void updateResponseEntity(ResponseEntity newEntity) {
        this.responseEntity.setFileType(newEntity.getFileType());
        this.responseEntity.setImageContent(newEntity.getImageContent());
        this.responseEntity.setAudioContent(newEntity.getAudioContent());
        this.responseEntity.setImageKeyword(newEntity.getImageKeyword());
        this.responseEntity.setAudioKeyword(newEntity.getAudioKeyword());
    }
}
