package com.diboot.ai.config;

/**
 * AiConfiguration 扩展配置
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
public interface AiConfigurator {

    /**
     * 扩展配置
     *
     * @param aiConfiguration
     */
    void configure(AiConfiguration aiConfiguration);

    /**
     * 执行优先级
     */
    default int getPriority() {
        return 999;
    }
}
