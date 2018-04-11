package com.eastrobot.converter.service;


import com.eastrobot.converter.model.ResponseMessage;
import org.springframework.web.multipart.MultipartFile;

/**
 * ConvertService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
public interface ConvertService {

    /**
     *
     * 上传文件
     *
     * @author Yogurt_lei
     * @date 2018-04-11 15:05
     */
    String doUpload(MultipartFile file, String sn) throws Exception;

    /**
     *
     * 传入资源绝对路径,开始解析
     *
     * @author Yogurt_lei
     * @date 2018-03-29 15:00
     */
    ResponseMessage driver(String resPath);
}
