package com.eastrobot.kbs.media.util.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 线程池工具类
 *
 *
 * @author <a href="yogurt_lei@foxmail.com">Yogurt_lei</a>
 * @version v1.0 , 2018-09-17 10:33
 */
@Slf4j
@Component
public class ThreadPoolUtil {
    private static final Map<ExecutorType, ExecutorService> THREAD_POOL_MAP = new HashMap<>(3);
    private static final int SHUTDOWN_TIMEOUT = 30;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int NCPU = Runtime.getRuntime().availableProcessors();

    @PostConstruct
    private void init() {
        //通用CPU密集型线程池
        if (!THREAD_POOL_MAP.containsKey(ExecutorType.GENERIC_CPU_INTENSIVE)) {
            AtomicInteger cpuThreadCounter = new AtomicInteger(1);
            ExecutorService executor = new ThreadPoolExecutor(NCPU, NCPU,
                    0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(NCPU),
                    r -> new Thread(r,
                            ExecutorType.GENERIC_CPU_INTENSIVE + "-POOL-" + cpuThreadCounter.getAndIncrement()),
                    // cpu密集型不会io阻塞,并行变串行,降低任务提交速度
                    new ThreadPoolExecutor.DiscardOldestPolicy());
            THREAD_POOL_MAP.put(ExecutorType.GENERIC_CPU_INTENSIVE, executor);
        }

        //通用IO密集型线程池-注意:io可能阻塞而导致主线程等待
        if (!THREAD_POOL_MAP.containsKey(ExecutorType.GENERIC_IO_INTENSIVE)) {
            AtomicInteger ioThreadCounter = new AtomicInteger(1);
            ExecutorService executor = new ThreadPoolExecutor(NCPU, 2 * NCPU + 10,
                    KEEP_ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<>(),
                    r -> new Thread(r,
                            ExecutorType.GENERIC_IO_INTENSIVE + "-POOL-" + ioThreadCounter.getAndIncrement()),
                    // io密集型可能会io阻塞,并行变串行,降低任务提交速度
                    // 在media中每段内容都是所需的,所以这里不抛弃任务,如果不在意完整性,可以考虑其他的拒绝策略
                    new ThreadPoolExecutor.CallerRunsPolicy());
            THREAD_POOL_MAP.put(ExecutorType.GENERIC_IO_INTENSIVE, executor);
        }
    }

    /**
     * 获取线程池
     */
    public static ExecutorService ofExecutor(ExecutorType type) {
        return THREAD_POOL_MAP.get(type);
    }

    /**
     * 异步关闭线程池
     *
     * @author Yogurt_lei
     * @date 2018-09-03 10:54
     */
    @PreDestroy
    private void destroy() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Map.Entry<ExecutorType, ExecutorService> entry : THREAD_POOL_MAP.entrySet()) {
                ExecutorType key = entry.getKey();
                ExecutorService exec = entry.getValue();
                exec.shutdown();
                try {
                    if (!exec.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS)) {
                        log.warn(key + "threadPool did not shutdown in the specified time.");
                        exec.shutdownNow();
                        log.warn(key + "threadPool was shutdownNow. all tasks will not be executed.");
                    } else {
                        log.info(key + "threadPool was shutdown complete.");
                    }
                } catch (InterruptedException e) {
                    log.error(key + "threadPool was shutdown occurred interrupted exception error...");
                }
            }
        }));
    }

}
