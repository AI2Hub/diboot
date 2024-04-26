package com.diboot.ai.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认 DefaultAiConfigurator
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
@Slf4j
public class DefaultAiConfigurator implements AiConfigurator{
    @Override
    public void configure(AiConfiguration aiConfiguration) {
        log.info("--------->  enabled  DefaultAiConfigurator");
    }
}
