package com.eastrobot.converter.service;

/**
 * ImageService
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 15:14
 */
public interface ImageService {

    /**
     * 解析视频为图片
     * <pre>
     *     ffmpeg -y -i {input} -r {fps} {output}
     * </pre>
     *
     * @author Yogurt_lei
     * @date 2018-03-26 14:36
     */
    Boolean runFfmpegParseImagesCmd(final String videoPath);

    /**
     *
     * 解析图片 生成文本
     *
     * @author Yogurt_lei
     * @date 2018-03-27 11:55
     */
    String handle(String imageFilePath);
}
