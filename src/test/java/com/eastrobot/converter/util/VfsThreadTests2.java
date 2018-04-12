/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.converter.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @date 2017年12月10日 下午5:06:38
 * @version 1.0
 */
public class VfsThreadTests2 {
	
	private final static Executor executor = Executors.newCachedThreadPool();
	
	public static void main(String[] args) throws InterruptedException {
		long start = Calendar.getInstance().getTimeInMillis();
		int max = 1024;
		CountDownLatch countDownLatch = new CountDownLatch(max);
		
		for (int i=0; i<max; i++){
			BigDecimal decimal = new BigDecimal(Math.random()*10000);
			String remoteFileName= Calendar.getInstance().getTimeInMillis() + "." + decimal.intValue() + "";
			VfsThread vfsThread = new VfsThread("E:/ConvertTester/TestFiles/why_always_me.txt", "/home/eko.zhan/lucy/tmp/" + remoteFileName + ".txt", countDownLatch);
			
			executor.execute(vfsThread);
		}
		
		countDownLatch.await();
		long end = Calendar.getInstance().getTimeInMillis();
		System.out.println("execute interval " + (end-start)/1000 + "s");
	}
}
