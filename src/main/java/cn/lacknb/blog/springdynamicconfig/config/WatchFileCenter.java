package cn.lacknb.blog.springdynamicconfig.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
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

    @Autowired
    private ConfigurableEnvironment environment;

    private static final Map<String, Object> SOURCES = new ConcurrentHashMap<>();


    private void refreshEnv() {
        // 使用 PropertiesLoaderUtils 加载属性文件
        try {

            // 创建一个 Properties 对象
            Properties properties = new Properties();
            ClassLoader classLoader = WatchFileCenter.class.getClassLoader();
            URL resource = classLoader.getResource("application.properties");
            String content = FileUtils.readFileToString(new File(resource.getPath()));
            // 使用 StringReader 将字符串读入 Properties 对象
            try (StringReader reader = new StringReader(content)) {
                // Resource resource = getRelativePathResource("E:\\JavaProject\\spring-dynamic-config\\src\\main\\resources",
                //         "application.properties");
                properties.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(content);
            // 用新加载的属性替换原有的属性源
            String key = "Config resource 'class path resource [application.properties]' via location 'optional:classpath:/'";
            environment.getPropertySources().replace(key,
                    new PropertiesPropertySource(key, properties));
            log.info("配置刷新~");
            // TODO 刷新Bean
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Resource getRelativePathResource(String parentPath, String path) {
        try {
            InputStream inputStream = Files.newInputStream(Paths.get(parentPath, path).toFile().toPath());
            return new InputStreamResource(inputStream);
        } catch (Exception ignore) {
        }
        return null;
    }

    @PostConstruct
    public void monitor() {
        try {
            this.watchService = FileSystems.getDefault().newWatchService();
            ClassLoader classLoader = WatchFileCenter.class.getClassLoader();
            URL resource = classLoader.getResource("application.properties");
            assert resource != null;
            File file = new File(resource.getPath());
            Path path = Paths.get(file.getParentFile().toURI());
            // String projectPath = System.getProperty("user.dir");
            // log.info(projectPath);
            // Path path = Paths.get(projectPath, "src/main/resources");
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
                                        refreshEnv();
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
