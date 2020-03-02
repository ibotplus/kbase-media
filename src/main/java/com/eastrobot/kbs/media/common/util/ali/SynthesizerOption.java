package com.eastrobot.kbs.media.common.util.ali;

import com.alibaba.nls.client.protocol.OutputFormatEnum;
import com.alibaba.nls.client.protocol.SampleRateEnum;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
@Accessors(chain = true)
public class SynthesizerOption {
    /**
     * 输出音频格式
     */
    private OutputFormatEnum outputFormat = OutputFormatEnum.MP3;

    /**
     * 输出音频采样率
     */
    private SampleRateEnum sampleRate;

    /**
     * 传入文本必须采用UTF-8编码；
     * 传入文本不能超过300个字符。超过300字符的内容会被截断，只合成300字符以内的内容
     */
    private String text;

    /**
     * 输出音频声音类型
     */
    private String voiceType;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

}
