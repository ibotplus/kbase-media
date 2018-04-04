package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.YouTuService;
import com.eastrobot.converter.util.ResUtil;
import com.eastrobot.converter.util.ffmpeg.FFmpeg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * ImageServiceImpl
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Service
public class ImageServiceImpl implements ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);


    @Value("${convert.video.vca.default}")
    private String VCA_TOOL;

    @Autowired
    private YouTuService youTuService;

    @Override
    public Boolean runFfmpegParseImagesCmd(final String videoPath) {
        String folder = ResUtil.getFolder(videoPath, "");

        FFmpeg fFmpeg = new FFmpeg("D:\\ffmpeg\\bin");
        fFmpeg.addParam("-y");
        fFmpeg.addParam("-i");
        fFmpeg.addParam(videoPath);
        fFmpeg.addParam("-r");
        fFmpeg.addParam("0.2");
        fFmpeg.addParam(folder + File.separator + "%005d.jpg");

        try {
            fFmpeg.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public String handle(String imageFilePath) {
        try {
            return youTuService.ocr(imageFilePath);
        } catch (Exception e) {
            logger.error("ocr [%s] failed : [%s]", imageFilePath, e.getMessage());
        }

        return "";
    }
}
