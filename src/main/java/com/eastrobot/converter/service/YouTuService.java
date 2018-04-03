package com.eastrobot.converter.service;


public interface YouTuService {

	/**
	 * 识别图片中的文字
	 * @param image_path  图片物理地址
	 * @return
	 */
	public String ocr(String image_path)throws Exception;
	
	/**
	 * 识别ulr图片中的文字
	 * @param image_url 图片url地址
	 * @return
	 */
	public String ocrUlr(String image_url)throws Exception;

}
