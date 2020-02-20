package com.eastrobot.kbs.media.model;

import com.eastrobot.kbs.media.model.aitype.AiType;
import lombok.*;

/**
 * ParseResult
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-04-20 17:56
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParseResult<T extends AiType> {
    /**
     * 错误码
     */
    private ResultCode code;
    /**
     * 结果包装
     */
    T result;
}
