/*
 * Power by www.xiaoi.com
 */
package com.eastrobot.kbs.media.web.controller;

import com.eastrobot.kbs.media.exception.BusinessException;
import com.eastrobot.kbs.media.model.ResponseMessage;
import com.eastrobot.kbs.media.model.ResultCode;
import com.eastrobot.kbs.media.service.ConvertService;
import com.eastrobot.kbs.media.util.youtu.YouTuOcrUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 黄色，暴力，恐怖图片识别接口
 *
 * @author <a href="mailto:eko.z@outlook.com">eko.zhan</a>
 * @version 1.0
 * @date 2019/3/7 13:38
 */
@Api(tags = "黄色，暴恐图片识别接口")
@Slf4j
@RestController()
@RequestMapping("/danger")
public class DangerController {

    @Resource
    private ConvertService converterService;

    @PostMapping("/porn")
    public ResponseMessage porn(MultipartFile file) throws Exception {
        Optional.ofNullable(file).filter(v -> !v.isEmpty()).orElseThrow(BusinessException::new);
        String md5 = DigestUtils.md5Hex(file.getBytes());
        String targetFile;
        try {
            targetFile = converterService.uploadFile(file, md5, false);
            return ResponseMessage.builder().message(YouTuOcrUtil.porn(targetFile)).build();
        } catch (Exception e) {
            return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILURE);
        }
    }

    @PostMapping("/terrorism")
    public ResponseMessage terrorism(MultipartFile file) throws Exception {
        Optional.ofNullable(file).filter(v -> !v.isEmpty()).orElseThrow(BusinessException::new);
        String md5 = DigestUtils.md5Hex(file.getBytes());
        String targetFile;
        try {
            targetFile = converterService.uploadFile(file, md5, false);
            return ResponseMessage.builder().message(YouTuOcrUtil.terrorism(targetFile)).build();
        } catch (Exception e) {
            return new ResponseMessage(ResultCode.FILE_UPLOAD_FAILURE);
        }
    }
}
