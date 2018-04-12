/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.converter.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @date 2017年12月10日 下午1:53:18
 * @version 1.0
 */
public class VfsThreadTests {

	private final static ExecutorService executor = Executors.newCachedThreadPool();
//	private final static ExecutorService executor = Executors.newSingleThreadExecutor();

	public static void main(String[] args) throws InterruptedException {
		long start = Calendar.getInstance().getTimeInMillis();
		for (int i=0; i<1024; i++){
			BigDecimal decimal = new BigDecimal(Math.random()*10000);
			String remoteFileName= Calendar.getInstance().getTimeInMillis() + "." + decimal.intValue() + "";
			VfsThread vfsThread = new VfsThread("E:/ConvertTester/TestFiles/why_always_me.txt", "/home/eko.zhan/lucy/tmp/" + remoteFileName + ".txt");
			
			executor.submit(vfsThread);
		}
		executor.shutdown();
		while (true){
			if (executor.isTerminated()){
				break;
			}
			Thread.sleep(1000);
		}
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("execute interval " + (end-start)/1000 + "s");
	}
}
