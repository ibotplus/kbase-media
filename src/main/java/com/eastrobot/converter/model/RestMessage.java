package com.eastrobot.converter.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * RestMessage
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-03 19:30
 */
// @Getter
// @Setter
// @ApiModel(description = "返回响应数据")
@Data
@XmlRootElement
public class RestMessage implements Serializable {
    // @ApiModelProperty(value = "是否成功")
    private Integer errorCode;
    private String errorMessage;

    public RestMessage(ErrorCode error) {
        this.errorCode = error.getCode();
        this.errorMessage = error.getMsg();
    }

    public RestMessage() {
        this.errorCode = ErrorCode.SUCCESS.getCode();
        this.errorMessage = ErrorCode.SUCCESS.getMsg();
    }

    public void setIErrorCode(ErrorCode error) {
        this.errorCode = error.getCode();
        this.errorMessage = error.getMsg();
    }
}
