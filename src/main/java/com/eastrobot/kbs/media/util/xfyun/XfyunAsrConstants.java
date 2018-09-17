package com.eastrobot.kbs.media.util.xfyun;

import com.iflytek.msp.cpdb.lfasr.model.LfasrType;

/**
 * XfyunAsrConstants
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-19 16:27
 */
public interface XfyunAsrConstants {
    /**
     * 标准版-已录制音频
     */
    LfasrType TYPE = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;
    /**
     * 上传成功
     */
    int UPLOAD_OK = 0;
    /**
     * 处理成功
     */
    int PROGRESS_OK = 0;
    int PROGRESS_COMPLETED = 9;
    int RESULT_COMPLETED = 9;
    /**
     * 每次获取进度的等待时长（秒）
     */
    int SLEEP_SECOND = 20;

    String ERROR_CODE = "ERROR_CODE";
    String MESSAGE = "MESSAGE";
    String SUCCESS = "0";
}
