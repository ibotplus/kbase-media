package com.eastrobot.kbs.media.web.controller;

import com.eastrobot.kbs.media.util.DataBakerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Api(tags = "转换接口(byte)")
@Slf4j
@RestController()
@RequestMapping("/convert-stream")
public class ByteController {

    @ApiOperation(value = "文本转语音", notes = "输入一段文本，返回一个音频文件流")
    @PostMapping(value = "/tts", produces = "audio/mp3")
    public void tts(@ApiParam(value = "参数为纯文本，如：我和我的祖国") @RequestBody String text, HttpServletResponse response) {
        log.debug("tts text: [{}]", text);
        byte[] tts = DataBakerUtil.tts(text);
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(tts));
        response.setHeader("Content-disposition", "attachment; filename=tts.mp3");
        try (ServletOutputStream sos = response.getOutputStream()) {
            IOUtils.copy(bis, sos);
            sos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
