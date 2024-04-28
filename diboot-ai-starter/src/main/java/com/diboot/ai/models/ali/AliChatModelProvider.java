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
package com.diboot.ai.models.ali;

import com.diboot.ai.config.AiConfiguration;
import com.diboot.ai.models.AbstractModelProvider;
import com.diboot.ai.models.ali.params.AliChatRequest;
import com.diboot.ai.models.ali.params.AliEnum;
import com.diboot.ai.models.ali.params.AliMessage;
import com.diboot.ai.request.AiChatRequest;
import com.diboot.ai.request.AiRequest;
import com.diboot.ai.request.AiRequestConvert;
import com.diboot.core.exception.BusinessException;
import com.diboot.core.util.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 阿里 通义千问 Provider
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Slf4j
public class AliChatModelProvider extends AbstractModelProvider implements AiRequestConvert<AiChatRequest, AliChatRequest> {

    /**
     * 对话APi
     */
    private static final String CHAT_API = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    public AliChatModelProvider(AiConfiguration configuration) {
        super(configuration,
                Arrays.asList(
                        AliEnum.Model.ALI_QWEN_TURBO.getCode(),
                        AliEnum.Model.ALI_QWEN_PLUS.getCode())
        );
    }

    @Override
    public void executeStream(AiRequest aiRequest, SseEmitter sseEmitter) {
        // 将通用参数 转化为 具体模型参数
        AliChatRequest aliChatRequest = convert((AiChatRequest) aiRequest);
        // 构建请求对象
        AliConfig aliConfig = configuration.getAliConfig();
        Request request = new Request.Builder()
                .url(CHAT_API)
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + aliConfig.getApikey())
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .post(RequestBody.Companion.create(JSON.toJSONString(aliChatRequest), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_VALUE)))
                .build();
        // 实例化EventSource，注册EventSource监听器
        // 创建一个用于处理服务器发送事件的实例，并定义处理事件的回调逻辑
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                try {
                    // 发送数据
                    sseEmitter.send(data);
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage());
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                sseEmitter.complete();
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                sseEmitter.completeWithError(t);
            }
        });
    }

    @Override
    public boolean supports(String model) {
        return supportModels.contains(model);
    }

    @Override
    public AliChatRequest convert(AiChatRequest source) {
        // 将消息转换aliMessage
        List<AliMessage> aliMessages = source.getMessages().stream()
                .map(message -> new AliMessage().setRole(message.getRole())
                        .setName(message.getName()).setContent(message.getContent())
                )
                .collect(Collectors.toList());
        // 转换请求
        return new AliChatRequest()
                .setModel(source.getModel())
                .setInput(new AliChatRequest.Input().setMessages(aliMessages));
    }
}
