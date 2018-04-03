package com.eastrobot.converter.util;

import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Properties;

public class SystemUtils {
	public static final String UTF8 = "UTF-8";
	public static final String OS_LINUX = "LINUX";

	/**
	 * 判断当前操作系统是 linux or windows
	 * @author eko.zhan at 2017年12月19日 下午6:20:54
	 * @return
	 */
	public static boolean isLinux(){
		Properties prop = System.getProperties();
		String defaultOS = prop.getProperty("os.name").toUpperCase();
		if (defaultOS.indexOf(OS_LINUX) > -1) {
			return true;
		}
		return false;
	}
	
	public static void loadStream(final InputStream input) {
		new Thread(new Runnable(){
			@Override
			public void run() {
				Reader reader = new InputStreamReader(input);
				BufferedReader bf = new BufferedReader(reader);
				String line = null;
				try {
					while((line=bf.readLine())!=null) {
						
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
    }
	
	public static String RealPath(){
		String realPath = "";
		try {
			File file = ResourceUtils.getFile("classpath:app.properties");
			realPath = file.getParent().replaceAll("[/\\\\]{1}WEB-INF[/\\\\]{1}classes[/\\\\]?", "").replace("%20", " ");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return realPath;
	}
}
