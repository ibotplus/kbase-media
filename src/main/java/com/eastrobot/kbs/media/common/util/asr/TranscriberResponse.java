package com.eastrobot.kbs.media.common.util.asr;

import lombok.Builder;
import lombok.Data;

/**
 * 转写结果
 *
 * @author yogurt_lei
 * @date 2019-10-23 10:51
 */
@Data
@Builder
public class TranscriberResponse {
    /**
     * 转写结果
     */
    private String msg;

    /**
     * 语速
     */
    private Integer speed;
}
