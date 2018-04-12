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
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @date 2017年12月9日 下午4:56:14
 * @version 1.0
 */
public class VFSTests {

	private String hostName = "172.16.9.55";
	private String port = "12598";
	private String username = "root";
	private String password = "508956";
	
	
	@Test
	public void upload() throws FileSystemException{
		String localFilePath = "E:/ConvertTester/TestFiles/v0.1.docx";
		String remoteFilePath = "/home/eko.zhan/lucy/tmp/v0.1.docx";
		File file = new File(localFilePath);
		
		StandardFileSystemManager manager = new StandardFileSystemManager();
		
		manager.init();

        // Create local file object
        FileObject localFile = manager.resolveFile(file.getAbsolutePath());

        // Create remote file object
        FileObject remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath, port), createDefaultOptions());
        /*
         * use createDefaultOptions() in place of fsOptions for all default
         * options - Ashok.
         */
        sftp://root:508956@172.16.9.55:12598/
        // Copy local file to sftp server
        remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);

        System.out.println("File upload success");
		
		
	}
	
	public static String createConnectionString(String hostName, String username, String password, String remoteFilePath, String port) {
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
