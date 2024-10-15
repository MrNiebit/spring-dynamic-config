package cn.lacknb.blog.springdynamicconfig.config;

import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

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
public class WatchFileCenter {

    private static final WatchService WATCH_SERVICE;

    static {
        try {
            WATCH_SERVICE = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
