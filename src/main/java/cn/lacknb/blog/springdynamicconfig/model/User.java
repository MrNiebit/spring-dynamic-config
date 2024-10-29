package cn.lacknb.blog.springdynamicconfig.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <h2>  </h2>
 *
 * @description:
 * @menu
 * @author: gitsilence
 * @description:
 * @date: 2024/10/29 23:28
 **/
@Data
public class User {

    private String account;

    private String name;

}
