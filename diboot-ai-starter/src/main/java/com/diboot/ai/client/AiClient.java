/*
 * Copyright (c) 2015-2029, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.diboot.ai.client;

import com.diboot.ai.config.AiConfiguration;
import com.diboot.ai.request.AiRequest;
import com.diboot.ai.models.ModelProvider;
import com.diboot.core.util.V;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI客户端
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Slf4j
public class AiClient {

    /**
     * AI 配置
     */
    @Getter
    private final AiConfiguration configuration;

    /**
     * 模型列表
     */
    private final List<ModelProvider> modelProviders;

    public AiClient(AiConfiguration configuration) {
        this.configuration = configuration;
        this.modelProviders = configuration.getModelProviders();
    }

    /**
     * 流式 执行
     *
     * @param aiRequest
     * @param sseEmitter
     * @return
     * @throws Exception
     */
    public void executeStream(AiRequest aiRequest, SseEmitter sseEmitter) throws Exception {
        if (V.isEmpty(this.modelProviders)) {
            sseEmitter.send("尚未启用模型服务");
        }
        sseEmitter.onError(throwable -> log.error("请求异常，当前正在使用模型：{}", aiRequest.getModel(), throwable));
        for (ModelProvider modelProvider : this.modelProviders) {
            if (!modelProvider.supports(aiRequest.getModel())) {
                continue;
            }
            modelProvider.executeStream(aiRequest, sseEmitter);
        }
        sseEmitter.send(aiRequest.getModel() + "无对应模型服务，请选择其他模型");
    }


}
