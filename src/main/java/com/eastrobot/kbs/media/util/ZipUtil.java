package com.eastrobot.kbs.media.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * zip 工具类
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-05-11 16:51
 */
@Slf4j
public class ZipUtil {
    private static final int BUFFER_SIZE = 1024;

    /**
     * 解压 zip 文件
     *
     * @param zipFile  zip 压缩文件
     * @param unzipDir 解压后存档路径
     *
     * @return 返回解压后文件名的绝对路径list
     */
    public static List<String> unZip(File zipFile, String unzipDir) {
        // 如果 destDir 不存在, 则解压到压缩文件所在目录
        if (StringUtils.isBlank(unzipDir)) {
            unzipDir = zipFile.getParent();
        }

        unzipDir = unzipDir.endsWith(File.separator) ? unzipDir : unzipDir + File.separator;

        List<String> fileNames = new ArrayList<>();

        try (
                ZipArchiveInputStream zais =
                        new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile), BUFFER_SIZE))
        ) {

            ZipArchiveEntry entry;
            while ((entry = zais.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    File directory = new File(unzipDir, entry.getName());
                    directory.mkdirs();
                } else {
                    fileNames.add(unzipDir + entry.getName());
                    try (
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(unzipDir, entry.getName())), BUFFER_SIZE)
                    ) {
                        IOUtils.copy(zais, os);
                    }
                }
            }
        } catch (Exception e) {
            log.error("unzip file {} occurred exception!");
        }

        return fileNames;
    }

    /**
     * 解压 zip 文件
     *
     * @param zipFile  zip 压缩文件的路径
     * @param unzipDir zip 压缩文件解压后保存的目录
     *
     * @return 返回解压后文件名的绝对路径list
     */
    public static List<String> unZip(String zipFile, String unzipDir) {
        return unZip(new File(zipFile), unzipDir);
    }

    /**
     * 解压zip文件到zip所在路径
     *
     * @param zipFile zip 压缩文件的路径
     *
     * @return 返回解压后文件名的绝对路径list
     */
    public static List<String> unZip(String zipFile) {
        return unZip(new File(zipFile), "");
    }

    /**
     * 解压zip文件到zip所在路径
     *
     * @param zipFile zip 压缩文件
     *
     * @return 返回解压后文件名的绝对路径list
     */
    public static List<String> unZip(File zipFile) {
        return unZip(zipFile, "");
    }
}
