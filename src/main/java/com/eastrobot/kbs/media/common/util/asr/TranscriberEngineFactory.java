package com.eastrobot.kbs.media.common.util.asr;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 构建实时转写引擎
 */
@Slf4j
public class TranscriberEngineFactory {

    private static Map<AsrEngine, ITranscriberEngine> services = new ConcurrentHashMap<>();

    public static ITranscriberEngine of(AsrEngine type) {
        return services.get(type);
    }

    public static void register(AsrEngine engine, ITranscriberEngine transcriber) {
        log.info("register transcriber : [{}]", engine);
        services.put(engine, transcriber);
    }
}
