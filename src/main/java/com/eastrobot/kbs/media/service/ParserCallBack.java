package com.eastrobot.kbs.media.service;

/**
 * ParserCallBack
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-16 15:12
 */
@FunctionalInterface
public interface ParserCallBack {
    String doInParser(final String fileName) throws Exception;
}
