package com.eastrobot.kbs.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.*;
import com.eastrobot.kbs.media.service.AudioService;
import com.eastrobot.kbs.media.service.ConvertService;
import com.eastrobot.kbs.media.service.ImageService;
import com.eastrobot.kbs.media.service.VideoService;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MultiMediaConverterServiceImpl
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-03-29 14:39
 */
@Slf4j
@Service
public class ConvertServiceImpl implements ConvertService {

    /**
     * 同步上传的文件夹
     */
    @Value("${convert.sync.output-folder}")
    private String SYNC_OUTPUT_FOLDER;
    /**
     * 异步上传的文件夹
     */
    @Value("${convert.async.output-folder}")
    private String ASYNC_OUTPUT_FOLDER;
    /**
     * 同步模式文件上传大小
     */
    @Value("${convert.sync.upload-file-size}")
    private String SYNC_FILE_UPLOAD_SIZE;
    /**
     * 异步模式文件上传大小
     */
    @Value("${convert.async.upload-file-size}")
    private String ASYNC_FILE_UPLOAD_SIZE;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private ImageService imageService;

    @Override
    public ResponseMessage driver(Map<String, Object> paramMap) {
        String resPath = null;
        ResponseMessage responseMessage = null;
        boolean asyncParse = MapUtils.getBoolean(paramMap, Constants.IS_ASYNC_PARSE, false);
        String aiType = MapUtils.getString(paramMap, Constants.AI_TYPE);

        if (Constants.RECOGNITION.equals(aiType)) {
            // 通用AI识别
            resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
            boolean isFrameExtractKeyword = MapUtils.getBoolean(paramMap, Constants.AI_IS_FRAME_EXTRACT_KEYWORD, false);
            String sn = FilenameUtils.getBaseName(resPath);

            if (ResourceUtil.isImage(resPath)) {
                ParseResult ocrResult = imageService.handle(resPath);
                responseMessage = new ResponseMessage<>(ocrResult.getCode(), sn, ocrResult.getResult());
            } else if (ResourceUtil.isAudio(resPath)) {
                ParseResult asrResult = audioService.handle(resPath);
                responseMessage = new ResponseMessage<>(asrResult.getCode(), sn, asrResult.getResult());
            } else if (ResourceUtil.isVideo(resPath)) {
                ParseResult vacParseResult = videoService.handle(resPath, isFrameExtractKeyword);
                responseMessage = new ResponseMessage<>(vacParseResult.getCode(), sn, vacParseResult.getResult());
            } else {
                responseMessage = new ResponseMessage(ResultCode.ILLEGAL_TYPE);
            }
        } else if (Constants.ASR.equals(aiType)) {
            resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
            String sn = FilenameUtils.getBaseName(resPath);
            if (ResourceUtil.isAudio(resPath)) {
                ParseResult asrResult = audioService.handle(resPath);
                responseMessage = new ResponseMessage<>(asrResult.getCode(), sn, asrResult.getResult());
            } else {
                responseMessage = new ResponseMessage(ResultCode.ILLEGAL_TYPE);
            }
        } else if (Constants.OCR.equals(aiType)) {
            resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
            String sn = FilenameUtils.getBaseName(resPath);
            if (ResourceUtil.isImage(resPath)) {
                ParseResult ocrResult = imageService.handle(resPath);
                responseMessage = new ResponseMessage<>(ocrResult.getCode(), sn, ocrResult.getResult());
            } else {
                responseMessage = new ResponseMessage(ResultCode.ILLEGAL_TYPE);
            }
        } else if (Constants.VAC.equals(aiType)) {
            resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
            boolean isFrameExtractKeyword = MapUtils.getBoolean(paramMap, Constants.AI_IS_FRAME_EXTRACT_KEYWORD, false);
            String sn = FilenameUtils.getBaseName(resPath);
            if (ResourceUtil.isVideo(resPath)) {
                ParseResult vacParseResult = videoService.handle(resPath, isFrameExtractKeyword);
                responseMessage = new ResponseMessage<>(vacParseResult.getCode(), sn, vacParseResult.getResult());
            } else {
                responseMessage = new ResponseMessage(ResultCode.ILLEGAL_TYPE);
            }
        } else if (Constants.TTS.equals(aiType)) {
            String text = MapUtils.getString(paramMap, Constants.AI_TTS_TEXT);
            Map ttsOption = MapUtils.getMap(paramMap, Constants.AI_TTS_OPTION, new HashMap());
            ParseResult ttsResult = audioService.handleTts(text, ttsOption);
            resPath = UUID.randomUUID().toString();
            responseMessage = new ResponseMessage<>(ttsResult.getCode(), resPath, ttsResult.getResult());
        }

        this.doWriteResultToFile(resPath, responseMessage, asyncParse);

        return responseMessage;
    }

    /**
     * 上传文件 同步异步是不同的文件夹
     * 异步上传的为zip或rar文件,其中包含具体要解析的文件
     * 同步模式 异步模式所限制文件大小不同
     *
     * @author Yogurt_lei
     * @date 2018-04-20 16:05
     */
    @Override
    public String uploadFile(MultipartFile file, String sn, boolean asyncParse) throws IOException, BusinessException {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        long fileSize = file.getSize();

        // 确保文件夹存在
        String targetFile = asyncParse ? ASYNC_OUTPUT_FOLDER : SYNC_OUTPUT_FOLDER;
        targetFile = targetFile + sn + "." + fileExtension;
        File tmpFile = new File(targetFile);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }

        if (asyncParse) {
            // 异步模式
            long allowBytes = ResourceUtil.parseMBorKBtoByte(ASYNC_FILE_UPLOAD_SIZE);
            if (fileSize > allowBytes) {
                throw new BusinessException("异步模式文件大小不能超过[" + ASYNC_FILE_UPLOAD_SIZE + "].");
            }

            // 解压zip到当前路径
            if (FileExtensionType.ZIP.ext().equals(fileExtension)) {
                file.transferTo(tmpFile);
                ZipUtil.unZip(tmpFile);
            } else {
                throw new BusinessException("异步模式上传文件需为ZIP文件");
            }
        } else {
            // 同步模式
            long allowBytes = ResourceUtil.parseMBorKBtoByte(SYNC_FILE_UPLOAD_SIZE);
            if (fileSize > allowBytes) {
                throw new BusinessException("同步模式文件大小不能超过[" + SYNC_FILE_UPLOAD_SIZE + "], 请访问异步接口.");
            }
            file.transferTo(tmpFile);
        }

        return targetFile;
    }


    /**
     * 解析结果写入文件:${convert.outputFolder}/sn.rs
     *
     * @param resPath         资源文件路径
     * @param responseMessage 响应结果
     * @param asyncParse      是否异步解析
     */
    private void doWriteResultToFile(String resPath, final ResponseMessage responseMessage, boolean asyncParse) {
        // 写到正确的文件夹下
        String resultFilePath = asyncParse ? ASYNC_OUTPUT_FOLDER : SYNC_OUTPUT_FOLDER;
        resultFilePath = resultFilePath + FilenameUtils.getBaseName(resPath) + FileExtensionType.RS.pExt();
        File resultFile = new File(resultFilePath);
        // 如果存在 说明是重复消费 不做处理
        if (!resultFile.exists()) {
            try (FileWriter fw = new FileWriter(resultFile)) {
                fw.write(responseMessage.toString());
            } catch (IOException e) {
                log.error("write responseMessage to file occurred exception.");
            }
        }
    }

    @Override
    public ResponseMessage findAsyncParseResult(String sn) {
        ResponseMessage responseMessage;
        String filePath = ASYNC_OUTPUT_FOLDER + sn + FileExtensionType.RS.pExt();
        File resultFile = new File(filePath);
        if (!resultFile.exists()) {
            responseMessage = new ResponseMessage(ResultCode.ASYNC_NOT_COMPLETED);
        } else {
            try (FileReader fr = new FileReader(resultFile);
                 BufferedReader br = new BufferedReader(fr)
            ) {
                StringBuilder lineBuilder = new StringBuilder("");
                String tmp;
                if ((tmp = br.readLine()) != null) {
                    lineBuilder.append(tmp);
                }
                responseMessage = JSON.parseObject(lineBuilder.toString(), ResponseMessage.class);
            } catch (IOException e) {
                log.warn("read result file occurred exception.");
                responseMessage = new ResponseMessage(ResultCode.ASYNC_READ_RESULT_FILE_FAILED);
            }
        }

        return responseMessage;
    }
}
