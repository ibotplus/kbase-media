package com.eastrobot.kbs.media.common.util.ali;

import java.io.File;

@FunctionalInterface
public interface SpeechSynthesizerCallBack {
    /**
     * @param targetFile 转换完毕后生成的目标文件
     */
    void callback(File targetFile);
}
