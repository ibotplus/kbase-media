package com.eastrobot.kbs.media.service;


import com.eastrobot.kbs.media.model.ResponseMessage;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * ConvertService
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
public interface ConvertService {

    /**
     * 上传文件 异步和同步上传到不同的文件夹
     * 文件名 md5.extension
     * <p>
     * 同步模式、异步模式所限制文件大小不同,上传文件夹不同,文件类型不同
     * 异步上传的为zip或rar文件,其中包含具体要解析的文件
     *
     * @param file  MultipartFile
     * @param md5   文件流md5
     * @param async 是否异步
     *
     * @return 上传完毕的资源文件路径
     *
     * @author Yogurt_lei
     * @date 2018-04-11 15:05
     */
    String uploadFile(MultipartFile file, String md5, boolean async) throws Exception;

    /**
     * 开始解析资源
     *
     * @param paramMap 参数map
     *
     * @author Yogurt_lei
     * @date 2018-03-29 15:00
     */
    ResponseMessage driver(Map<String, Object> paramMap);


    /**
     * 查找解析的结果
     *
     * @param md5   文件md5
     * @param async 是否异步
     *
     * @author Yogurt_lei
     * @date 2018-04-12 19:59
     */
    ResponseMessage findParseResultByMd5(String md5, boolean async);
}
