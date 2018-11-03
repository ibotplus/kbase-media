package com.eastrobot.kbs.media.util.xfyun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eastrobot.kbs.media.model.Constants;
import com.iflytek.msp.cpdb.lfasr.client.LfasrClientImp;
import com.iflytek.msp.cpdb.lfasr.exception.LfasrException;
import com.iflytek.msp.cpdb.lfasr.model.Message;
import com.iflytek.msp.cpdb.lfasr.model.ProgressStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * IflytekAsrUtil
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-19 16:09
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "convert", name = "video.asr.default", havingValue = Constants.XFYUN)
public class XfyunAsrUtil {

    private static LfasrClientImp lfasrClient;

    @PostConstruct
    private void init() {
        try {
            lfasrClient = LfasrClientImp.initLfasrClient();
        } catch (LfasrException e) {
            log.error("initialize xfyun occurred exception.", e);
        }
    }

    /**
     * {
     * "bg":250,    \\当前这句话的说话开始时间，单位为毫秒
     * "ed":2890,  \\当前这句话的说话结束时间，单位为毫秒
     * "onebest":"噢，你，你听得到我这边的声音吗？",
     * "speaker":"1" \\说话人编号（数字“1”和“2”为不同说话人，电话专用版功能）
     * }﻿
     * see http://www.xfyun.cn/doccenter/lfasr#go_sdk_doc_v2
     */
    public static JSONObject asr(String path) {
        JSONObject resultJson = new JSONObject();

        HashMap<String, String> params = new HashMap<>();
        // params.put("has_participle", "true");
        // params.put("not_wait", "true");

        String taskId = "";
        try {
            // 上传音频文件
            Message uploadMsg = lfasrClient.lfasrUpload(path, XfyunAsrConstants.TYPE, params);
            if (uploadMsg.getOk() == XfyunAsrConstants.UPLOAD_OK) {
                taskId = uploadMsg.getData();
            } else {
                // 创建任务失败-服务端异常
                resultJson.put(XfyunAsrConstants.ERROR_CODE, uploadMsg.getErr_no());
                resultJson.put(XfyunAsrConstants.MESSAGE, uploadMsg.getFailed());

                return resultJson;
            }
        } catch (LfasrException e) {
            log.error("upload asr file to xfyun server occurred exception", e);
        }

        // 循环等待音频处理结果
        while (true) {
            try {
                Thread.sleep(XfyunAsrConstants.SLEEP_SECOND * 1000);
                log.warn("task {} waiting for xfyun result.", taskId);
            } catch (InterruptedException e) {
                // do nothing
            }

            try {
                // 获取处理进度
                Message progressMsg = lfasrClient.lfasrGetProgress(taskId);

                if (progressMsg.getOk() != XfyunAsrConstants.PROGRESS_OK) {
                    log.error("task {} was fail.", taskId);
                    // 服务端处理异常-服务端内部有重试机制（不排查极端无法恢复的任务）
                    // 客户端可根据实际情况选择：
                    // 1. 客户端循环重试获取进度
                    // 2. 退出程序，反馈问题

                    resultJson.put(XfyunAsrConstants.ERROR_CODE, progressMsg.getErr_no());
                    resultJson.put(XfyunAsrConstants.MESSAGE, progressMsg.getFailed());

                    return resultJson;
                } else {
                    ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
                    if (progressStatus.getStatus() == XfyunAsrConstants.PROGRESS_COMPLETED) {
                        // 处理完成
                        log.info("task {} was completed. ", taskId);
                        break;
                    } else {
                        // 未处理完成
                        log.warn("task {} was incomplete. current status {}", taskId, progressStatus.getDesc());
                    }
                }
            } catch (LfasrException e) {
                // 获取进度异常处理，根据返回信息排查问题后，再次进行获取
                Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
                resultJson.put(XfyunAsrConstants.ERROR_CODE, progressMsg.getErr_no());
                resultJson.put(XfyunAsrConstants.MESSAGE, progressMsg.getFailed());

                return resultJson;
            }
        }

        // 获取任务结果
        try {
            Message resultMsg = lfasrClient.lfasrGetResult(taskId);
            log.info("task {} result is [{}].", taskId, resultMsg.getData());
            if (resultMsg.getOk() == XfyunAsrConstants.RESULT_COMPLETED) {
                String onebest = JSON.parseObject(resultMsg.getData()).getString("onebest");

                resultJson.put(XfyunAsrConstants.ERROR_CODE, XfyunAsrConstants.SUCCESS);
                resultJson.put(XfyunAsrConstants.MESSAGE, onebest.trim());

                return resultJson;
            } else {
                // 转写失败，根据失败信息进行处理
                resultJson.put(XfyunAsrConstants.ERROR_CODE, resultMsg.getErr_no());
                resultJson.put(XfyunAsrConstants.MESSAGE, resultMsg.getFailed());

                return resultJson;
            }
        } catch (LfasrException e) {
            // 获取结果异常处理，解析异常描述信息
            Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
            resultJson.put(XfyunAsrConstants.ERROR_CODE, resultMsg.getErr_no());
            resultJson.put(XfyunAsrConstants.MESSAGE, resultMsg.getFailed());

            return resultJson;
        }
    }
}
