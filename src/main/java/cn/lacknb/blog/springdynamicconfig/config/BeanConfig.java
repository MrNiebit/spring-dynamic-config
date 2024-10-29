package cn.lacknb.blog.springdynamicconfig.config;

import cn.lacknb.blog.springdynamicconfig.model.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h2>  </h2>
 *
 * @description:
 * @menu
 * @author: gitsilence
 * @description:
 * @date: 2024/10/29 23:32
 **/
@Configuration
@EnableConfigurationProperties
public class BeanConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "user")
    public User user() {
        return new User();
    }

}
