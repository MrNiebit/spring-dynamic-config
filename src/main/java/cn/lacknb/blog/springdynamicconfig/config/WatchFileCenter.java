package cn.lacknb.blog.springdynamicconfig.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h2>  </h2>
 *
 * @description:
 * @menu
 * @author: nbh
 * @description:
 * @date: 2024/10/15 9:25
 **/
@Configuration
@Slf4j
public class WatchFileCenter {

    private WatchService watchService;
    private ExecutorService executor;

    final AtomicLong lastProcessedTime = new AtomicLong(0);
    final long debounceInterval = 1000; // 1 秒

    @PostConstruct
    public void monitor() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            String projectPath = System.getProperty("user.dir");
            log.info(projectPath);
            Path path = Paths.get(projectPath, "src/main/resources");
            log.info(path.toString());
            if (!Files.exists(path)) {
                System.out.println("指定目录不存在~");
                return;
            }
            path.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            this.executor = Executors.newFixedThreadPool(1, (r) -> {
                Thread t = new Thread(r, "file-monitor-");
                t.setDaemon(true);
                return t;
            });

            this.executor.execute(() -> {
                while (!Thread.interrupted()) {
                    try {
                        WatchKey watchKey = this.watchService.take();
                        List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
                        watchKey.reset();
                        for (WatchEvent<?> watchEvent : watchEvents) {
                            WatchEvent.Kind<?> kind = watchEvent.kind();
                            log.info(kind.name() + " -> " + watchEvent.context());
                            Path changed = (Path) watchEvent.context();
                            String changedFileName = changed.getFileName().toString();
                            if (kind == StandardWatchEventKinds.ENTRY_MODIFY && "application.properties".equals(changedFileName)) {
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastProcessedTime.get() > debounceInterval) {
                                    if (lastProcessedTime.compareAndSet(lastProcessedTime.get(), currentTime)) {
                                        log.info("application.properties 文件发生修改，触发配置刷新");
                                        // TODO: 实现配置刷新逻辑
                                    }
                                } else {
                                    log.debug("忽略过于频繁的修改事件: {}", changedFileName);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        log.warn("中断线程...");
                    } catch (ClosedWatchServiceException ex) {

                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException {
        if (this.watchService != null) {
            watchService.close();
        }
        executor.shutdown();
    }
}
