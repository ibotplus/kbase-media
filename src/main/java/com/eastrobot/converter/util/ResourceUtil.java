package com.eastrobot.converter.util;

import com.eastrobot.converter.util.ffmpeg.FFmpeg;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ResourceUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 9:23
 */
public class ResourceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);
    public static final String IMAGE = "1";
    public static final String PLACE = "2";
    public static final String AUDIO = "3";
    public static final String FILE = "4";
    public static final String VIDEO = "5";

    /**
     * 获取允许上传的资源类型
     *
     * @author Yogurt_lei
     * @date 2018-03-26 10:51
     */
    public static List getAllowFileType() {
        return Arrays.asList(IMAGE, PLACE, AUDIO, VIDEO, FILE);
    }

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
     * 文件内容按时间升序排序
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:47
     */
    public static File[] orderByDate(File[] fs) {
        Arrays.sort(fs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                long diff = f2.lastModified() - f1.lastModified();
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }
        });

        return fs;
    }

    /**
     * 文件内容按名称升序排序
     *
     * @author Yogurt_lei
     * @date 2018-03-26 18:47
     */
    public static File[] orderByName(File[] fs) {
        Arrays.sort(fs, new Comparator<File>() {
            public int compare(File f1, File f2) {
                int diff = f1.getName().compareTo(f2.getName());
                if (diff > 0)
                    return 1;
                else if (diff == 0)
                    return 0;
                else
                    return -1;
            }

            public boolean equals(Object obj) {
                return true;
            }
        });

        return fs;
    }

    /**
     *
     * 文件上传后处理
     *
     * 视频文件:若是其他视频格式 则转成MP4
     * 图片文件:适当缩放为大(xPixel 2倍)中(mPixel 3倍)小(sPixel 4倍)三类 存于images/[id]/下
     *
     * @author Yogurt_lei
     * @date 2018-03-28 20:07
     */
    public static String afterFileUpload(String resPath) {
        if (!SystemUtils.isLinux()) {
            resPath = StringUtils.substringAfter(resPath, "/");
        }

        if (isVideo(resPath)) {
            if (FilenameUtils.getExtension(resPath).equalsIgnoreCase("mov")
                    || FilenameUtils.getExtension(resPath).equalsIgnoreCase("flv")
                    || FilenameUtils.getExtension(resPath).equalsIgnoreCase("avi")) {
                String originResPath = resPath;
                String newFileName = FilenameUtils.getFullPath(resPath) + FilenameUtils.getBaseName(resPath) + ".mp4";
                try {
                    FFmpeg fFmpeg = new FFmpeg(PropertiesUtil.getString("ocr.ffmpeg"));
                    //TODO 指定视频编码格式（ 带有 H.264 视频编码和 AAC 音频编码）：-acodec aac -vcodec h264
                    fFmpeg.addParam("-y").addParam("-i").addParam(originResPath).addParam(newFileName).execute();

                    File file = new File(originResPath);
                    file.delete();

                    resPath = newFileName;
                } catch (IOException e) {
                    LOGGER.error("upload [%s] video parse to *.mp4 occured error:[%s]", e.getMessage());
                }
            }
        } else if (isImage(resPath)) {
            String folder = getFolder(resPath, "");

            try {
                FFmpeg fFmpeg = new FFmpeg(PropertiesUtil.getString("ocr.ffmpeg"));
                //缩放2倍生成xPixel
                fFmpeg.addParam("-y").addParam("-i").addParam(resPath).addParam("-vf").addParam("\"scale=256:256/a\"")
                        .addParam(folder + "xPixel." + FilenameUtils.getExtension(resPath));
                fFmpeg.execute();

                //缩放3倍生成mPixel
                fFmpeg.addParam("-y").addParam("-i").addParam(resPath).addParam("-vf").addParam("\"scale=128:128/a\"")
                        .addParam(folder + "mPixel." + FilenameUtils.getExtension(resPath));
                fFmpeg.execute();

                //缩放4倍生成sPixel
                fFmpeg.addParam("-y").addParam("-i").addParam(resPath).addParam("-vf").addParam("\"scale=64:64/a\"")
                        .addParam(folder + "sPixel." + FilenameUtils.getExtension(resPath));
                fFmpeg.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resPath.substring(resPath.indexOf("/DATAS/"));
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
    public static Boolean isVideo(String filename){
        return ArrayUtils.contains(videoArray, FilenameUtils.getExtension(filename).toLowerCase());
    }
    /**
     * 是否是图片文件
     */
    public static Boolean isImage(String filename){
        return ArrayUtils.contains(imageArray, FilenameUtils.getExtension(filename).toLowerCase());
    }

    /**
     * 是否是声音文件
     */
    public static Boolean isAudio(String filename){
        return ArrayUtils.contains(audioArray, FilenameUtils.getExtension(filename).toLowerCase());
    }

    /**
     *
     * 根据key排序 value拼接split字符串返回
     *
     * @author Yogurt_lei
     * @date 2018-03-29 20:20
     */
    public static String map2SortStringByKey(Map<String, String> map, String split) {
        TreeMap<String, String> sortMap = map2SortByKey(map);

        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            result.append(entry.getValue()).append(split);
        }

        return result.toString();
    }


    /**
     *
     * 根据key排序 返回有序TreeMap
     *
     * @author Yogurt_lei
     * @date 2018-03-29 20:20
     */
    public static TreeMap<String, String> map2SortByKey(Map<String, String> map) {
        TreeMap<String, String> sortMap = new TreeMap<>(String::compareTo);
        sortMap.putAll(map);

        return sortMap;
    }
}
