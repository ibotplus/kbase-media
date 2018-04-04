package com.eastrobot.converter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.MultiMediaConverterService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

/**
 * MultiMediaConverterServiceImpl
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
@Service
public class MultiMediaConverterServiceImpl implements MultiMediaConverterService {

    @Value("${convert.output-folder}")
    private String OUTPUT_FOLDER;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private ImageService imageService;

    @Override
    public String getDefaultOutputFolderPath() {
        return OUTPUT_FOLDER + File.separator + DateFormatUtils.format(new Date(), "yyyyMMdd");
    }

    @Override
    public JSONObject driver(String resPath) {
        JSONObject json = new JSONObject();
        File resFile = new File(resPath);

        String text = new String();

        if (ResUtil.isVideo(resPath) || ResUtil.isAudio(resPath)) {
            JSONObject videoJSON = videoService.parseVideo(resPath);
            String audioContent = videoJSON.getString("audios");
            String imageKeyWord = videoJSON.getString("images");

            json.put("flag", "success");
            json.put("file_type", ResUtil.isVideo(resPath) ? "video" : "audio");
            if (StringUtils.isNotBlank(audioContent)) {
                json.put("content", audioContent);
            }
            if (StringUtils.isNotBlank(imageKeyWord)) {
                json.put("keyword", imageKeyWord);
            }
            if (StringUtils.isBlank(audioContent) && StringUtils.isBlank(imageKeyWord)) {
                json.put("flag", "failed");
                json.put("err_code","1001");
                json.put("err_msg", "empty result");
            }

        } else if (ResUtil.isImage(resPath)) {
            String imageContent = imageService.handle(resPath);

            json.put("flag", "success");
            json.put("file_type", "image");
            json.put("content", imageContent);
            json.put("keyword", imageContent);
        } else {
            json.put("flag","failed");
            json.put("err_code","1000");
            json.put("err_msg", "cannot parse this format file" + resPath);
        }


        return json;
    }
}
