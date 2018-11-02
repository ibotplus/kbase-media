package com.eastrobot.kbs.media.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

/**
 * ResourceUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 9:23
 */
@Slf4j
public class ResourceUtil {

    /**
     * 获取parentPath/parentName/文件夹路径, 若不存在则创建
     *
     * @param parentPath 父路径
     *
     * @return path parentPath/parentName/ or "" (empty, mark create directory failed)
     *
     * @author Yogurt_lei
     * @date 2018-03-26 11:46
     */
    public static String ofFileNameFolder(String parentPath) {
        try {
            return Files
                    .createDirectories(
                            Paths.get(FilenameUtils.getFullPath(parentPath), FilenameUtils.getBaseName(parentPath))
                    ).toString();
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("Files.createDirectories {}", parentPath);
        }

        return "";
    }

    /**
     * 将数组转换成字符串
     *
     * @author eko.zhan at 2017年12月21日 下午1:18:51
     */
    public static String list2String(Collection<String> list, final String split) {
        return list.stream().reduce((a, b) -> a + split + b).orElse("");
    }

    private static String[] videoArray = new String[]{"avi", "asf", "wmv", "avs", "flv", "mkv", "mov", "3gp", "mp4",
            "mpg", "mpeg", "dat", "ogm", "vob", "rm", "rmvb", "ts", "tp", "ifo", "nsv"};
    private static String[] imageArray = new String[]{"jpg", "bmp", "eps", "gif", "mif", "miff", "png", "tif", "tiff",
            "svg", "wmf", "jpe", "jpeg", "dib", "ico", "tga", "cut", "pic"};
    private static String[] audioArray = new String[]{"mp3", "aac", "wav", "wma", "cda", "flac", "m4a", "mid", "mka",
            "mp2", "mpa", "mpc", "ape", "ofr", "ogg", "ra", "wv", "tta", "ac3", "dts"};

    /**
     * 是否是视频文件
     */
    public static Boolean isVideo(String filename) {
        return ArrayUtils.contains(videoArray, FilenameUtils.getExtension(filename).toLowerCase());
    }

    /**
     * 是否是图片文件
     */
    public static Boolean isImage(String filename) {
        return ArrayUtils.contains(imageArray, FilenameUtils.getExtension(filename).toLowerCase());
    }

    /**
     * 是否是声音文件
     */
    public static Boolean isAudio(String filename) {
        return ArrayUtils.contains(audioArray, FilenameUtils.getExtension(filename).toLowerCase());
    }

    /**
     * 根据key正序 value拼接split字符串返回
     *
     * @author Yogurt_lei
     * @date 2018-03-29 20:20
     */
    public static String map2SortByKeyAndMergeWithSplit(Map<Integer, String> map, final String split) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .reduce((a, b) -> a + split + b)
                .orElse("");
    }

    /**
     * 将 10MB or 10KB 转换为字节数
     */
    public static long parseMBorKBtoByte(String size) {
        size = size.toUpperCase(Locale.ENGLISH);
        if (size.endsWith("KB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024;
        }
        if (size.endsWith("MB")) {
            return Long.valueOf(size.substring(0, size.length() - 2)) * 1024 * 1024;
        }
        return Long.valueOf(size);
    }
}
