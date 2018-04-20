package com.eastrobot.converter.service.impl;

import com.eastrobot.converter.model.*;
import com.eastrobot.converter.service.AudioService;
import com.eastrobot.converter.service.ConvertService;
import com.eastrobot.converter.service.ImageService;
import com.eastrobot.converter.service.VideoService;
import com.eastrobot.converter.util.ResourceUtil;
import com.hankcs.hanlp.HanLP;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Optional;

import static com.eastrobot.converter.model.ResultCode.*;

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
    @Value("${convert.outputFolder}")
    private String OUTPUT_FOLDER;
    /**
     * 异步上传的文件夹
     */
    @Value("${convert.outputFolder-async}")
    private String OUTPUT_FOLDER_ASYNC;

    @Autowired
    private VideoService videoService;

    @Autowired
    private AudioService audioService;

    @Autowired
    private ImageService imageService;

    @Override
    public ResponseMessage driver(String resPath, boolean asyncParse) {
        ResponseMessage responseMessage;
        String sn = FilenameUtils.getBaseName(resPath);

        if (ResourceUtil.isAudio(resPath)) {
            ParseResult asrResult = audioService.handle(resPath);
            responseMessage = this.doResultToResponseMessage(sn, asrResult, Constants.AUDIO);
        } else if (ResourceUtil.isVideo(resPath)) {
            VacParseResult vacParseResult = videoService.handle(resPath);
            responseMessage = this.doResultToResponseMessage(sn, vacParseResult, Constants.VIDEO);
        } else if (ResourceUtil.isImage(resPath)) {
            ParseResult ocrResult = imageService.handle(resPath);
            responseMessage = this.doResultToResponseMessage(sn, ocrResult, Constants.IMAGE);
        } else {
            responseMessage = new ResponseMessage(ILLEGAL_TYPE);
        }

        this.doWriteResultToFile(resPath, responseMessage, asyncParse);

        return responseMessage;
    }

    @Override
    public ResponseMessage findAsyncParseResult(String sn) {
        String filePath = OUTPUT_FOLDER_ASYNC + sn + FileType.RS.getExtensionWithPoint();
        File resultFile = new File(filePath);
        ResponseMessage responseMessage = new ResponseMessage(ResultCode.SUCCESS);
        responseMessage.setSn(sn);
        try (FileReader fr = new FileReader(resultFile);
             BufferedReader br = new BufferedReader(fr)
        ) {
            String line;
            ResponseEntity entity = new ResponseEntity();
            if ((line = br.readLine()) != null) {
                if (line.startsWith(Constants.ERROR_MSG)) {
                    responseMessage.setMessage(line);
                } else if (line.startsWith(Constants.IMAGE_KEYWORD)) {
                    entity.setImageKeyword(line);
                } else if (line.startsWith(Constants.AUDIO_KEYWORD)) {
                    entity.setAudioKeyword(line);
                } else if (line.startsWith(Constants.IMAGE_CONTENT)) {
                    entity.setImageContent(line);
                } else if (line.startsWith(Constants.AUDIO_CONTENT)) {
                    entity.setAudioContent(line);
                }
            }
            responseMessage.setResponseEntity(entity);
        } catch (FileNotFoundException e) {
            log.warn("convert is not complete.");
            responseMessage.setResultCode(ResultCode.NOT_COMPLETED);
        } catch (IOException e) {
            log.warn("read result file occurred exception.");
            responseMessage.setResultCode(ResultCode.ASYNC_READ_RESULT_FILE_FAILED);
        }

        return responseMessage;
    }

    /**
     *
     * 上传文件 同步异步是不同的文件夹
     *
     * @author Yogurt_lei
     * @date 2018-04-20 16:05
     */
    @Override
    public String doUpload(MultipartFile file, String sn, boolean asyncParse) throws Exception {
        String targetFile = asyncParse ? OUTPUT_FOLDER_ASYNC : OUTPUT_FOLDER;
        targetFile = targetFile + sn + "." + FilenameUtils.getExtension(file.getOriginalFilename());
        File tmpFile = new File(targetFile);
        tmpFile.mkdirs();
        file.transferTo(tmpFile);

        return targetFile;
    }

    /**
     * 解析结果封装到ResponseMessage
     * @param sn 序列号
     * @param parseResult 解析结果
     * @param type 文件类型 {@link Constants}
     */
    private ResponseMessage doResultToResponseMessage(String sn, AbstractParseResult parseResult, String type) {
        ResponseMessage message = new ResponseMessage();
        message.setSn(sn);
        switch (type) {
            case Constants.IMAGE: {
                ParseResult ocrResult = (ParseResult) parseResult;
                message.setResultCode(ocrResult.getCode());

                ResponseEntity entity = new ResponseEntity();
                entity.setImageContent(ocrResult.getResult());
                message.setResponseEntity(entity);
            }
            break;
            case Constants.AUDIO: {
                ParseResult asrResult = (ParseResult) parseResult;
                message.setResultCode(asrResult.getCode());

                ResponseEntity entity = new ResponseEntity();
                entity.setAudioContent(asrResult.getResult());
                message.setResponseEntity(entity);
            }
            break;
            case Constants.VIDEO: {
                VacParseResult vacParseResult = (VacParseResult) parseResult;
                ParseResult ocrResult = vacParseResult.getOcrParseResult();
                ParseResult asrResult = vacParseResult.getAsrParseResult();

                if (ocrResult.getCode().equals(OCR_PART_PARSE_FAILED) || asrResult.getCode().equals(ASR_PART_PARSE_FAILED)) {
                    message.setResultCode(PART_PARSE_FAILED);
                    Optional.ofNullable(ocrResult.getMessage()).ifPresent(s ->  message.setMessage(ocrResult.getMessage()));
                    Optional.ofNullable(asrResult.getMessage()).ifPresent(s ->  message.setMessage(message.getMessage()+""+asrResult.getMessage()));
                } else {
                    message.setResultCode(ResultCode.SUCCESS);
                }

                ResponseEntity entity = new ResponseEntity();
                entity.setImageContent(ocrResult.getResult());
                entity.setAudioContent(asrResult.getResult());
                message.setResponseEntity(entity);
            }
            break;
            default:
                break;
        }
        return message;
    }

    /**
     * 解析结果写入文件:${convert.outputFolder}/sn.rs
     */
    private void doWriteResultToFile(String resPath, final ResponseMessage responseMessage, boolean asyncParse) {
        // 写到正确的文件夹下
        String resultFilePath = asyncParse ? OUTPUT_FOLDER_ASYNC : OUTPUT_FOLDER;
        resultFilePath = resultFilePath + FilenameUtils.getBaseName(resPath) + FileType.RS.getExtensionWithPoint();
        File resultFile = new File(resultFilePath);

        // write error message
        if (!responseMessage.getCode().equals(SUCCESS.getCode())) {
            String errorMessage = responseMessage.getMessage();
            if (StringUtils.isNotBlank(errorMessage)) {
                try (FileWriter fw = new FileWriter(resultFile, true)) {
                    fw.write(Constants.ERROR_MSG + errorMessage + "\r\n");
                } catch (IOException e) {
                    log.error("write errorMessage to file occurred exception.");
                }
            }
        }

        //extract image keyword and write to file
        Optional.of(responseMessage)
                .map(ResponseMessage::getResponseEntity)
                .map(ResponseEntity::getImageContent)
                .ifPresent((content) -> {
                    String keyword = ResourceUtil.list2String(HanLP.extractKeyword(content, 10), "");
                    if (StringUtils.isNotBlank(keyword)) {
                        try (FileWriter fw = new FileWriter(resultFile, true)) {
                            fw.write(Constants.IMAGE_CONTENT + content + "\r\n");
                            fw.write(Constants.IMAGE_KEYWORD + keyword + "\r\n");
                        } catch (IOException e) {
                            log.error("write image keyword to file occurred exception.");
                        }
                        responseMessage.getResponseEntity().setImageKeyword(keyword);
                    }
                });
        //extract audio keyword and write to file
        Optional.of(responseMessage)
                .map(ResponseMessage::getResponseEntity)
                .map(ResponseEntity::getAudioContent)
                .ifPresent((content) -> {
                    String keyword = ResourceUtil.list2String(HanLP.extractKeyword(content, 10), "");
                    if (StringUtils.isNotBlank(keyword)) {
                        try (FileWriter fw = new FileWriter(resultFile, true)) {
                            fw.write(Constants.AUDIO_CONTENT + content + "\r\n");
                            fw.write(Constants.AUDIO_KEYWORD + keyword + "\r\n");
                        } catch (IOException e) {
                            log.error("write audio keyword to file occurred exception.");
                        }
                        responseMessage.getResponseEntity().setAudioKeyword(keyword);
                    }
                });
    }
}
