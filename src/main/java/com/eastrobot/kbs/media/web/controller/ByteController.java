package com.eastrobot.kbs.media.web.controller;

import com.eastrobot.kbs.media.util.DataBakerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "转换接口(byte)")
@Slf4j
@RestController()
@RequestMapping("/convert-stream")
public class ByteController {

    @ApiOperation("文本转语音")
    @ApiResponse(code = 200, message = "tts语音生成成功.", responseHeaders = {
            @ResponseHeader(name = "Content-disposition", description = "attachment; filename=download.mp3")
    })
    @GetMapping(value = "/tts", produces = "audio/mp3")
    public ResponseEntity<byte[]> tts(@RequestParam String text) {
        return ResponseEntity.ok(DataBakerUtil.tts(text));
    }
}
