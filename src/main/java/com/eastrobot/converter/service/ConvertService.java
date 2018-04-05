package com.eastrobot.converter.service;


import com.alibaba.fastjson.JSONObject;

/**
 * ConvertService
 *
 * @author <a href="yogurt.lei@xiaoi.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
public interface ConvertService {
    /**
     *
     * 获得当天默认转换的输出文件夹
     *
     * @author Yogurt_lei
     * @date 2018-03-29 14:41
     */
    String getDefaultOutputFolderPath();

    /**
     *
     * 传入资源绝对路径,开始解析
     *
     * @author Yogurt_lei
     * @date 2018-03-29 15:00
     */
    JSONObject driver(String resPath);
}
