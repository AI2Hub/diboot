package com.diboot.ai.config;

import com.diboot.ai.models.ali.AliConfig;
import lombok.Getter;
import lombok.Setter;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Ai 可扩展配置
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "diboot.ai")
public class AiProperties {

    /**
     * okhttp 客户端日志级别
     * <p>
     * NONE：不记录任何日志。
     * BASIC：记录请求类型、URL、响应状态码以及响应时间。
     * HEADERS：记录请求和响应的头部信息，以及BASIC级别的信息。
     * BODY：记录请求和响应的头部信息、body内容，以及BASIC级别的信息。注意，记录body内容可能会消耗资源，并且会读取body数据，这可能会影响请求的执行。
     */
    private HttpLoggingInterceptor.Level httpLoggingLevel = HttpLoggingInterceptor.Level.BASIC;

    /**
     * 阿里模型配置
     */
    @NestedConfigurationProperty
    private AliConfig aliConfig;

}
