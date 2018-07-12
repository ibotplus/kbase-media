package com.eastrobot.converter.service;


import com.eastrobot.converter.model.ResponseMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

/**
 * ConvertService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
public interface ConvertService {

    /**
     * 上传文件 异步和同步上传到不同的文件夹
     * 文件名 sn.extension
     *
     * @author Yogurt_lei
     * @date 2018-04-11 15:05
     */
    String uploadFile(MultipartFile file, String sn, boolean asyncParse) throws Exception;

    /**
     * 开始解析资源
     *
     * @param paramMap 参数map
     *
     * @author Yogurt_lei
     * @date 2018-03-29 15:00
     */
    ResponseMessage driver(HashMap<String, Object> paramMap);


    /**
     * 查找异步解析的结果
     *
     * @author Yogurt_lei
     * @date 2018-04-12 19:59
     */
    ResponseMessage findAsyncParseResult(String sn);
}
