package com.eastrobot.kbs.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.*;
import com.eastrobot.kbs.media.model.aitype.ASR;
import com.eastrobot.kbs.media.model.aitype.OCR;
import com.eastrobot.kbs.media.model.aitype.TTS;
import com.eastrobot.kbs.media.model.aitype.VAC;
import com.eastrobot.kbs.media.service.AudioService;
import com.eastrobot.kbs.media.service.ConvertService;
import com.eastrobot.kbs.media.service.ImageService;
import com.eastrobot.kbs.media.service.VideoService;
import com.eastrobot.kbs.media.util.ResourceUtil;
import com.eastrobot.kbs.media.util.ZipUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

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
    private String syncOutputFolder;
    /**
     * 异步上传的文件夹
     */
    @Value("${convert.async.output-folder}")
    private String asyncOutputFolder;
    /**
     * 同步模式文件上传大小
     */
    @Value("${convert.sync.upload-file-size}")
    private String syncFileUploadSize;
    /**
     * 异步模式文件上传大小
     */
    @Value("${convert.async.upload-file-size}")
    private String asyncFileUploadSize;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private ImageService imageService;

    @Override
    public String uploadFile(MultipartFile file, String md5, boolean async) throws IOException, BusinessException {
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        long fileSize = file.getSize();

        // 确保文件夹存在
        String targetFile = async ? asyncOutputFolder : syncOutputFolder;
        File tmpFile = new File(targetFile + md5 + "." + fileExtension);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
            if (async) {
                // 异步模式
                long allowBytes = ResourceUtil.parseMBorKBtoByte(asyncFileUploadSize);
                if (fileSize > allowBytes) {
                    throw new BusinessException("异步模式文件大小不能超过[" + asyncFileUploadSize + "].");
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
                long allowBytes = ResourceUtil.parseMBorKBtoByte(syncFileUploadSize);
                if (fileSize > allowBytes) {
                    throw new BusinessException("同步模式文件大小不能超过[" + syncFileUploadSize + "], 请访问异步接口.");
                }
                file.transferTo(tmpFile);
            }
        }

        return tmpFile.getAbsolutePath();
    }

    @Override
    public ResponseMessage driver(Map<String, Object> paramMap) {
        String resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
        String md5 = FilenameUtils.getBaseName(resPath);
        boolean async = MapUtils.getBoolean(paramMap, Constants.IS_ASYNC_PARSE, false);
        AiType aiType = (AiType) MapUtils.getObject(paramMap, Constants.AI_TYPE);

        // 检查是否已经转换过
        ResponseMessage responseMessage = this.findParseResultByMd5(md5, async);
        if (Optional.ofNullable(responseMessage).map(ResponseMessage::getResponseEntity).isPresent()) {
            return responseMessage;
        } else {
            switch (aiType) {
                case GENERIC_RECOGNITION:
                case ASR:
                case OCR:
                case VAC:
                    responseMessage = this.aiRecognition(aiType, paramMap);
                    break;
                case GENERIC_SYNTHESIS:
                case TTS:
                    responseMessage = this.aiSynthesis(aiType, paramMap);
                    break;
                default:
                    responseMessage = new ResponseMessage(ResultCode.ILLEGAL_TYPE);
                    break;
            }

            this.doWriteResultToFile(resPath, responseMessage, async);
            return responseMessage;
        }
    }

    @Override
    public ResponseMessage findParseResultByMd5(String md5, boolean async) {
        ResponseMessage resp = null;
        String filePath = async ? asyncOutputFolder : syncOutputFolder + md5 + FileExtensionType.RS.pExt();
        File resultFile = new File(filePath);
        if (!resultFile.exists()) {
            resp = new ResponseMessage(ResultCode.NOT_COMPLETED);
        } else {
            try {
                String content = Files.lines(resultFile.toPath(), Charset.defaultCharset()).reduce("", (a, b) -> a + b);
                JSONObject contentJson = Optional.ofNullable(content)
                        .filter(StringUtils::isNotBlank)
                        .map(JSON::parseObject)
                        .orElseThrow(BusinessException::new);

                switch (AiType.valueOf(contentJson.getString("aiType"))) {
                    case ASR:
                        resp = JSONObject.parseObject(content, new TypeReference<ResponseMessage<ASR>>() {
                        });
                        break;
                    case OCR:
                        resp = JSONObject.parseObject(content, new TypeReference<ResponseMessage<OCR>>() {
                        });
                        break;
                    case VAC:
                        resp = JSONObject.parseObject(content, new TypeReference<ResponseMessage<VAC>>() {
                        });
                        break;
                    case TTS:
                        resp = JSONObject.parseObject(content, new TypeReference<ResponseMessage<TTS>>() {
                        });
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.warn("read result file occurred exception:" + e.getMessage());
                resp = new ResponseMessage(ResultCode.READ_RESULT_FILE_FAILURE);
            }
        }

        return resp;
    }

    /**
     * ai识别
     */
    private ResponseMessage aiRecognition(AiType aiType, Map<String, Object> paramMap) {
        String resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
        String md5 = FilenameUtils.getBaseName(resPath);
        ParseResult parseResult = null;

        if (AiType.GENERIC_RECOGNITION.equals(aiType)) {
            if (ResourceUtil.isImage(resPath)) {
                aiType = AiType.OCR;
            } else if (ResourceUtil.isAudio(resPath)) {
                aiType = AiType.ASR;
            } else if (ResourceUtil.isVideo(resPath)) {
                aiType = AiType.VAC;
            }
        }

        // 明细解析
        if (AiType.ASR.equals(aiType)) {
            parseResult = audioService.handle(resPath, paramMap);
        } else if (AiType.OCR.equals(aiType)) {
            parseResult = imageService.handle(resPath, paramMap);
        } else if (AiType.VAC.equals(aiType)) {
            parseResult = videoService.handle(resPath, paramMap);
        }

        if (parseResult == null) {
            return new ResponseMessage(ResultCode.ILLEGAL_TYPE);
        } else {
            return ResponseMessage.builder()
                    .aiType(aiType)
                    .code(parseResult.getCode().code())
                    .md5(md5)
                    .responseEntity(parseResult.getResult())
                    .build();
        }
    }

    /**
     * ai合成
     */
    private ResponseMessage aiSynthesis(AiType aiType, Map<String, Object> paramMap) {
        String resPath = MapUtils.getString(paramMap, Constants.AI_RESOURCE_FILE_PATH);
        String md5 = FilenameUtils.getBaseName(resPath);
        String text = MapUtils.getString(paramMap, Constants.AI_TTS_TEXT);
        Map ttsOption = MapUtils.getMap(paramMap, Constants.AI_TTS_OPTION);

        ParseResult ttsResult = audioService.handleTts(text, ttsOption);
        return ResponseMessage.builder()
                .aiType(aiType)
                .code(ttsResult.getCode().code())
                .md5(md5)
                .responseEntity(ttsResult.getResult())
                .build();
    }

    /**
     * 解析结果写入文件:${convert.outputFolder}/sn.rs
     *
     * @param resPath         资源文件路径
     * @param responseMessage 响应结果
     * @param async           是否异步
     */
    private void doWriteResultToFile(String resPath, final ResponseMessage responseMessage, boolean async) {
        // 写到正确的文件夹下
        String resultFilePath = async ? asyncOutputFolder : syncOutputFolder;
        File resultFile = new File(resultFilePath + FilenameUtils.getBaseName(resPath) + FileExtensionType.RS.pExt());
        // 如果存在 说明是重复消费 不做处理
        if (!resultFile.exists()) {
            try (FileWriter fw = new FileWriter(resultFile)) {
                fw.write(responseMessage.toString());
            } catch (IOException e) {
                log.error("write responseMessage to file occurred exception.");
            }
        }
    }
}
