package com.elasticsearch.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author zhumingli
 * @create 2018-08-23 下午11:06
 * @desc 文件上传配置
 **/
@Data
@ConfigurationProperties(prefix = "fileupload")
@Component
public class FileuploadConfig {

    private String path;
}
