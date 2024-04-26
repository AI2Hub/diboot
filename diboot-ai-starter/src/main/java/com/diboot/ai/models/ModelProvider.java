package com.diboot.ai.models;

import com.diboot.ai.request.AiRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 模型拦截器
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
public interface ModelProvider {

    /**
     * 流式 执行器
     *
     * @param aiRequest
     * @param sseEmitter
     * @return
     */
    void executeStream(AiRequest aiRequest, SseEmitter sseEmitter);

    /**
     * 当前拦截器是否支持模型
     *
     * @param model 模型名称
     * @return
     */
    boolean supports(String model);
}
