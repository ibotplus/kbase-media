package com.eastrobot.kbs.media.model;

/**
 * VAC_TYPE
 */
public enum VacType {
    /**
     * 视频识别，包含图片帧和音轨
     */
    VAC,
    /**
     * 视频识别，仅识别图片帧
     */
    VAC_OCR,
    /*
     * 视频识别，仅识别音轨
     */
    VAC_ASR,
}
