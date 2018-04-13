package com.eastrobot.converter.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * ResourceUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 9:23
 */
public class ResourceUtil {

    /**
     * 获取文件夹路径, 若不存在则以文件名创建新的子文件夹
     *
     * @author Yogurt_lei
     * @date 2018-03-26 11:46
     */
    public static String getFolder(String inputPath, String subPath) {
        String folder = FilenameUtils.getFullPath(inputPath) + FilenameUtils.getBaseName(inputPath) + File.separator
                + subPath;

        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }

        return folder;
    }

    /**
     * 将数组转换成字符串
     *
     * @author eko.zhan at 2017年12月21日 下午1:18:33
     */
    public static String list2String(Collection<String> list) {
        return list2String(list, " ");
    }

    /**
     * 将数组转换成字符串
     *
     * @author eko.zhan at 2017年12月21日 下午1:18:51
     */
    public static String list2String(Collection<String> list, String split) {
        StringBuffer sb = new StringBuffer();
        for (String s : list) {
            sb.append(s + split);
        }
        return sb.toString();
    }

    /**
     * 获取项目根路径 结尾不含/
     *
     * @author Yogurt_lei
     * @date 2018-03-29 10:03
     */
    public static String getRootPath() {
        String path = ResourceUtil.class.getResource("/").getPath();
        if (SystemUtils.isLinux()) {
            return StringUtils.substringBefore(path, "/WEB-INF/classes/");
        } else {
            return StringUtils.substringBetween(path, "/", "/WEB-INF/classes/");
        }
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
    public static String map2SortByKey(Map map, String split) {
        TreeMap<Comparable, String> sortMap = map2SortByKey(map);

        StringBuilder result = new StringBuilder();
        for (Map.Entry<Comparable, String> entry : sortMap.entrySet()) {
            result.append(entry.getValue()).append(split);
        }

        return result.toString();
    }

    /**
     * 根据key正序 返回有序TreeMap
     *
     * @author Yogurt_lei
     * @date 2018-03-29 20:20
     */
    public static TreeMap map2SortByKey(Map<Comparable, String> map) {
        TreeMap sortMap = new TreeMap<>((Comparator<Comparable>) Comparable::compareTo);
        sortMap.putAll(map);

        return sortMap;
    }
}
