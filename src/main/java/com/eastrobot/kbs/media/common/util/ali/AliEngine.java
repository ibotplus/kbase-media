package com.eastrobot.kbs.media.common.util.ali;

import com.alibaba.nls.client.protocol.InputFormatEnum;
import com.alibaba.nls.client.protocol.NlsClient;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriber;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberListener;
import com.alibaba.nls.client.protocol.asr.SpeechTranscriberResponse;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizer;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerListener;
import com.alibaba.nls.client.protocol.tts.SpeechSynthesizerResponse;
import com.eastrobot.kbs.media.config.ConvertAsrProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

/**
 * 阿里引擎工具类
 *
 * @author yogurt_lei
 * @date 2019-09-09 15:09
 */
@Slf4j
@Component
public class AliEngine {
    private static String APP_KEY = "default";
    private static NlsClient nlsClient;
    private static String CUSTOMIZATION_ID = "";
    private static final String MALE_VOICE = "Sicheng";
    @Resource
    private ConvertAsrProperties convertAsrProperties;

    @PostConstruct
    public void init() {
        ConvertAsrProperties.AliNls aliNls = convertAsrProperties.getAliNls();
        APP_KEY = aliNls.getAppKey();
        nlsClient = new NlsClient(aliNls.getServerIp(), aliNls.getAccessToken());
        CUSTOMIZATION_ID = aliNls.getCustomizationId();
    }

    public static NlsClient ofClient() {
        return nlsClient;
    }

    /**
     * 实时语音转写，初始化转录器，实现回调接口实现具体业务需求逻辑
     */
    @SneakyThrows
    public static SpeechTranscriber speechTranscribe(SpeechTranscriberCallBack speechTranscriberCallBack) {
        SpeechTranscriber transcriber = new SpeechTranscriber(nlsClient, new SpeechTranscriberListener() {
            //识别出中间结果.服务端识别出一个字或词时会返回此消息.仅当setEnableIntermediateResult(true)时,才会有此类消息返回
            @Override
            public void onTranscriptionResultChange(SpeechTranscriberResponse response) {
                log.debug("onTranscriptionResultChange >> task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        //状态码 20000000 表示正常识别
                        ", status: " + response.getStatus() +
                        //句子编号，从1开始递增
                        ", index: " + response.getTransSentenceIndex() +
                        //当前的识别结果
                        ", result: " + response.getTransSentenceText() +
                        //当前已处理的音频时长，单位是毫秒
                        ", time: " + response.getTransSentenceTime());

                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onTranscriptionResultChange, response);
            }

            @Override
            public void onTranscriberStart(SpeechTranscriberResponse response) {
                log.debug("onTranscriberStart >> task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        ", status: " + response.getStatus());

                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onTranscriberStart, response);
            }

            @Override
            public void onSentenceBegin(SpeechTranscriberResponse response) {
                log.debug("onSentenceBegin >> task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        ", status: " + response.getStatus());
                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onSentenceBegin, response);
            }

            //识别出一句话.服务端会智能断句,当识别到一句话结束时会返回此消息
            @Override
            public void onSentenceEnd(SpeechTranscriberResponse response) {
                log.debug("onSentenceEnd >> task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        //状态码 20000000 表示正常识别
                        ", status: " + response.getStatus() +
                        //句子编号，从1开始递增
                        ", index: " + response.getTransSentenceIndex() +
                        //当前的识别结果
                        ", result: " + response.getTransSentenceText() +
                        //置信度
                        ", confidence: " + response.getConfidence() +
                        //开始时间
                        ", begin_time: " + response.getSentenceBeginTime() +
                        //当前已处理的音频时长，单位是毫秒
                        ", time: " + response.getTransSentenceTime());

                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onSentenceEnd, response);
            }

            //识别完毕
            @Override
            public void onTranscriptionComplete(SpeechTranscriberResponse response) {
                log.debug("onTranscriptionComplete >> task_id: " + response.getTaskId() +
                        ", name: " + response.getName() +
                        ", status: " + response.getStatus() + " ==> " + response.getTransSentenceText());
                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onTranscriptionComplete, response);
            }

            @Override
            public void onFail(SpeechTranscriberResponse response) {
                log.error(
                        "onFail >> task_id: " + response.getTaskId() +
                                //状态码 20000000 表示识别成功
                                ", status: " + response.getStatus() +
                                //错误信息
                                ", status_text: " + response.getStatusText());
                speechTranscriberCallBack.callback(SpeechTranscriberResponseType.onFail, response);
            }

        });
        transcriber.setAppKey(APP_KEY);
        // 输入音频编码方式
        transcriber.setFormat(InputFormatEnum.PCM);
        // 输入音频采样率
        transcriber.setSampleRate(SampleRateEnum.SAMPLE_RATE_16K);
        // 是否返回中间识别结果
        transcriber.setEnableIntermediateResult(true);
        // 是否生成并返回标点符号
        transcriber.setEnablePunctuation(true);
        // 是否将返回结果规整化,比如将一百返回为100
        transcriber.setEnableITN(true);
        // asr转写调用模板
        transcriber.addCustomedParam("customization_id", CUSTOMIZATION_ID);
        // 语义断句
        transcriber.addCustomedParam("enable_semantic_sentence_detection", true);
        return transcriber;
    }

    /**
     * 语音合成， 实现回调接口实现具体业务需求逻辑
     */
    @SneakyThrows
    public static void speechSynthesize(SynthesizerOption option, SpeechSynthesizerCallBack callBack) {
        log.debug("speech synthesize: {}", option);
        SpeechSynthesizer synthesizer = new SpeechSynthesizer(nlsClient, new SpeechSynthesizerListener() {
            File f = Paths.get(System.getProperty("java.io.tmpdir"), System.currentTimeMillis() + ".mp3").toFile();
            FileOutputStream fout = new FileOutputStream(f);

            @Override
            @SneakyThrows
            public void onComplete(SpeechSynthesizerResponse response) {
                log.info("name: " + response.getName() + ", status: " + response.getStatus() + ", output file :" + f.getAbsolutePath());
                if (fout != null) {
                    fout.flush();
                    fout.close();
                }
                callBack.callback(f);
            }

            @Override
            public void onFail(SpeechSynthesizerResponse response) {
                log.error(
                        "task_id: " + response.getTaskId() +
                                //状态码 20000000 表示识别成功
                                ", status: " + response.getStatus() +
                                //错误信息
                                ", status_text: " + response.getStatusText());
            }

            @Override
            @SneakyThrows
            public void onMessage(ByteBuffer message) {
                byte[] bytesArray = new byte[message.remaining()];
                message.get(bytesArray, 0, bytesArray.length);
                fout.write(bytesArray);
            }
        });

        //创建实例,建立连接
        synthesizer.setAppKey(APP_KEY);
        //设置返回音频的编码格式
        synthesizer.setFormat(option.getOutputFormat());
        //设置返回音频的采样率
        synthesizer.setSampleRate(option.getSampleRate());
        //发音人
        synthesizer.setVoice(option.getVoiceType());
        // 男声语速语调调整
        if (MALE_VOICE.equals(option.getVoiceType())) {
            //语调，范围是-500~500，可选，默认是0
            synthesizer.setPitchRate(-4);
            //语速，范围是-500~500，默认是0
            synthesizer.setSpeechRate(100);
        }
        //设置用于语音合成的文本
        synthesizer.setText(option.getText());
        //此方法将以上参数设置序列化为json发送给服务端,并等待服务端确认
        synthesizer.start();
        //等待语音合成结束
        synthesizer.waitForComplete();
        callBack.callback(null);
        //关闭连接
        synthesizer.close();
    }
}
