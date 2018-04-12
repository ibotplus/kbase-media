/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.converter.util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @date 2017年12月10日 下午12:01:43
 * @version 1.0
 */
public class VfsThread extends Thread {

	private static final String hostName = "172.16.9.55";
	private static final String port = "12598";
	private static final String username = "root";
	private static final String password = "508956";
	
	private String localFilePath = "";
	private String remoteFilePath = "";
	private CountDownLatch countDownLatch = null;
	
	private static StandardFileSystemManager manager = null;
	
	private synchronized static StandardFileSystemManager getSystemManager(){
		if (manager==null){
			try {
				manager = new StandardFileSystemManager();
				manager.init();
			} catch (FileSystemException e) {
				e.printStackTrace();
			}
		}
		return manager;
	}
	
	public VfsThread(String localFilePath, String remoteFilePath){
		this.localFilePath = localFilePath;
		this.remoteFilePath = remoteFilePath;
	}
	
	public VfsThread(String localFilePath, String remoteFilePath, CountDownLatch countDownLatch){
		this.localFilePath = localFilePath;
		this.remoteFilePath = remoteFilePath;
		this.countDownLatch = countDownLatch;
	}
	
	@Override
	public void run() {
		super.run();
		
		try {
			manager = getSystemManager();
			FileObject localFile = manager.resolveFile(localFilePath);
			FileObject remoteFile = manager.resolveFile(createConnectionString(remoteFilePath), createDefaultOptions());
			remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
		} catch (FileSystemException e) {
			e.printStackTrace();
		} finally {
			if (countDownLatch!=null){
				countDownLatch.countDown();
			}
		}

        System.out.println("[" + Thread.currentThread().getName() + "] File " + remoteFilePath + " upload success");
	}
	
	public static String createConnectionString(String remoteFilePath) {
        return "sftp://" + username + ":" + password + "@" + hostName + ":" + port + "/" + remoteFilePath;
    }
	
	public static FileSystemOptions createDefaultOptions() throws FileSystemException {
        // Create SFTP options
        FileSystemOptions opts = new FileSystemOptions();

        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");

        /*
         * Using the following line will cause VFS to choose File System's Root
         * as VFS's root. If I wanted to use User's home as VFS's root then set
         * 2nd method parameter to "true"
         */
        // Root directory set to user home
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

        // Timeout is count by Milliseconds
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);

        return opts;
    }
}
