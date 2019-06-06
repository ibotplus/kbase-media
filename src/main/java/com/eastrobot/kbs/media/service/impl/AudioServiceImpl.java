package com.eastrobot.kbs.media.service.impl;

import com.eastrobot.kbs.media.model.Constants;
import com.eastrobot.kbs.media.model.FileExtensionType;
import com.eastrobot.kbs.media.model.ParseResult;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.service.AudioService;
import com.eastrobot.kbs.media.service.ParserCallBack;
import com.eastrobot.kbs.media.util.ChineseUtil;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.baidu.BaiduAsrUtils;
import com.eastrobot.kbs.media.util.concurrent.ExecutorType;
import com.eastrobot.kbs.media.util.concurrent.ThreadPoolUtil;
import com.eastrobot.kbs.media.util.ffmpeg.FFmpegUtil;
import com.eastrobot.kbs.media.util.shhan.ShhanAsrUtil;
import com.eastrobot.kbs.media.util.xfyun.XfyunAsrConstants;
import com.eastrobot.kbs.media.util.xfyun.XfyunAsrUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.eastrobot.kbs.media.util.baidu.BaiduAsrConstants.PCM;
import static com.eastrobot.kbs.media.util.baidu.BaiduAsrConstants.RATE;


/**
 * AudioServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-26 18:54
 */
@Slf4j
@Service
public class AudioServiceImpl implements AudioService {

    @Value("${convert.audio.asr.default}")
    private String audioTool;

    @Override
    public ParseResult<ASR> handle(String audioFilePath, Map paramMap) {
        if (Constants.BAIDU.equals(audioTool)) {
            return AudioParserTemplate.handle(audioFilePath, 60, this::baiduAsrHandler);
        } else if (Constants.SHHAN.equals(audioTool)) {
            return AudioParserTemplate.handle(audioFilePath, 20, this::shhanAsrHandler);
        } else if (Constants.XFYUN.equals(audioTool)) {
            return AudioParserTemplate.handle(audioFilePath, 60, this::xfyunAsrHandler);
        } else {
            return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
        }
    }

    private String baiduAsrHandler(String audioFilePath) throws Exception {
        JSONObject asr = BaiduAsrUtils.asr(audioFilePath, PCM, RATE);
        if (asr.optInt("err_no", -1) == 0) {
            //数组字符串
            String result = asr.optString("result");
            return StringUtils.substringBetween(result, "[\"", "\"]").trim();
        } else {
            throw new Exception(asr.getString("err_msg"));
        }
    }

    private String shhanAsrHandler(String audioFilePath) throws Exception {
        String asr = ShhanAsrUtil.asr(audioFilePath);
        if (StringUtils.isNotBlank(asr)) {
            return asr;
        } else {
            throw new Exception("empty result");
        }
    }

    private String xfyunAsrHandler(String audioFilePath) throws Exception {
        JSONObject asr = XfyunAsrUtil.asr(audioFilePath);
        if (asr.optString(XfyunAsrConstants.ERROR_CODE).equals(XfyunAsrConstants.SUCCESS)) {
            return asr.optString(XfyunAsrConstants.MESSAGE);
        } else {
            throw new Exception(asr.toString());
        }
    }

    /**
     * 音频解析器模版
     */
    private static class AudioParserTemplate {

        /**
         * 解析音频的前置准备, 切割分段文件 然后调用具体解析器
         *
         * @param audioFilePath   音频文件
         * @param segmentDuration 针对各个解析器,所支持的每段文件最大时长
         * @param callBack        具体解析器回调
         */
        private static ParseResult<ASR> handle(String audioFilePath, long segmentDuration, ParserCallBack callBack) {
            try {
                // 1. 是否切割文件 {segmentDuration} 每段,文件放入当前文件夹下 filename-%d.pcm
                FFmpegUtil.splitSegFileToPcm(audioFilePath, segmentDuration);
            } catch (IOException e) {
                log.error("splitSegToPcm occurred exception, check the ffmpeg location is right.");
                return new ParseResult<>(ResultCode.ASR_FAILURE, null);
            }

            // 得到所有待解析pcm

            File[] allPcmFiles = Paths.get(ResourceUtil.ofFileNameFolder(audioFilePath))
                    .toFile()
                    .listFiles(filename -> filename.getName().startsWith(FilenameUtils.getBaseName(audioFilePath))
                            && filename.getName().endsWith(FileExtensionType.PCM.ext()));

            if (Objects.requireNonNull(allPcmFiles).length > 1) {
                return multiSegAudioFileParse(allPcmFiles, callBack);
            } else {
                try {
                    String resultText = callBack.doInParser(allPcmFiles[0].getAbsolutePath());
                    if (StringUtils.isNotBlank(resultText)) {
                        List<String> keywords = HanLP.extractKeyword(resultText, 100);
                        String keyword = ResourceUtil.list2String(keywords, ",");
                        return new ParseResult<>(ResultCode.SUCCESS, new ASR(resultText, keyword));
                    }

                    return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
                } catch (Exception e) {
                    log.warn("asrHandler parse audio occurred exception: {}", e.getMessage());
                    return new ParseResult<>(ResultCode.ASR_FAILURE, null);
                }
            }
        }

        /**
         * 解析多段音频的文件
         */
        private static ParseResult<ASR> multiSegAudioFileParse(File[] allPcmFiles, ParserCallBack callBack) {
            // 2. 遍历pcm文件 解析每段文本
            ExecutorService executor = ThreadPoolUtil.ofExecutor(ExecutorType.GENERIC_IO_INTENSIVE);
            int taskCount = allPcmFiles.length;
            // 总任务数门阀
            CountDownLatch latch = new CountDownLatch(taskCount);
            // 存储音轨解析段-内容
            ConcurrentHashMap<Integer, String> audioContentMap = new ConcurrentHashMap<>(taskCount);
            AtomicBoolean hasOccurredException = new AtomicBoolean(false);
            for (File file : allPcmFiles) {
                String filePath = file.getAbsolutePath();
                //提交音轨转文字任务
                log.debug("asrHandler executor submit asrTask {}", filePath);
                executor.submit(() -> {
                    String baseName = FilenameUtils.getBaseName(filePath);
                    String currentSegIndex = StringUtils.substringAfterLast(baseName, "-");
                    try {
                        String content = callBack.doInParser(filePath);
                        log.debug("asrHandler parse {} result : {}", filePath, content);
                        audioContentMap.put(Integer.parseInt(currentSegIndex), content);
                    } catch (Exception e) {
                        log.warn("asrHandler parse seg audio {} occurred exception: {}", baseName, e.getMessage());
                        hasOccurredException.set(true);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await(taskCount * 5, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                hasOccurredException.set(true);
                log.error("asrHandler parse audio thread occurred exception : {}", e.getMessage());
            }

            // 2. 解析结束后 合并内容 提取关键字
            String resultText = ResourceUtil.map2SortByKeyAndMergeWithSplit(audioContentMap, "");
            if (StringUtils.isNotBlank(resultText)) {
                resultText = ChineseUtil.removeMessy(resultText);
                List<String> keywords = HanLP.extractKeyword(resultText, 100);
                String keyword = ResourceUtil.list2String(keywords, ",");
                if (hasOccurredException.get()) {
                    return new ParseResult<>(ResultCode.PART_PARSE_FAILURE, new ASR(resultText, keyword));
                } else {
                    return new ParseResult<>(ResultCode.SUCCESS, new ASR(resultText, keyword));
                }
            } else {
                return new ParseResult<>(ResultCode.PARSE_EMPTY, null);
            }
        }
    }
}
