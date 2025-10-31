package com.ollamaService.lazyOperation;


import com.ollamaService.model.mongoCollections.mcp.McpApplicationErrorLogs;
import com.ollamaService.model.repo.mcp.McpToolCallHistoryRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by: Sharan MH
 * on: 21/08/25
 */

@Component
public class MongoLazyOperation {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);//max 100 operations
    private static final Logger log = LoggerFactory.getLogger(MongoLazyOperation.class);

    public MongoLazyOperation(McpToolCallHistoryRepository repo) {
        startBackgroundWorker();
    }

    public void pushAndForget(Runnable runnableOperation) {
        if (!queue.offer(runnableOperation))
            log.error("severity={} LazyOperation no space in queue (max_size: 100) for more operation: offered operation rejected", McpApplicationErrorLogs.Severity.HIGH);
    }

    private void startBackgroundWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = queue.take(); // wait for a task
                    task.run(); // execute update
                } catch (Exception e) {
                    log.error("severity={} Error while flushing lazy task", McpApplicationErrorLogs.Severity.HIGH, e);
                }
            }
        });
        worker.setDaemon(true);//tell jvm not to wait
        worker.start();
    }

    @PreDestroy
    public void flushBeforeShutdown() {
        log.info("Spring context closing. Flushing remaining tasks...");
        Runnable task;
        while ((task = queue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("severity={} Error while flushing lazy task", McpApplicationErrorLogs.Severity.HIGH, e);
            }
        }
    }
}
