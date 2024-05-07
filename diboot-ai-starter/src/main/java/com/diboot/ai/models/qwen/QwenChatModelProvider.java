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
package com.diboot.ai.models.qwen;

import com.diboot.ai.common.AiMessage;
import com.diboot.ai.common.request.AiChatRequest;
import com.diboot.ai.common.request.AiEnum;
import com.diboot.ai.common.request.AiRequest;
import com.diboot.ai.common.request.AiRequestConvert;
import com.diboot.ai.common.response.AiChatResponse;
import com.diboot.ai.common.response.AiResponse;
import com.diboot.ai.common.response.AiResponseConvert;
import com.diboot.ai.config.AiConfiguration;
import com.diboot.ai.models.AbstractModelProvider;
import com.diboot.core.util.BeanUtils;
import com.diboot.core.util.JSON;
import com.diboot.core.util.V;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSourceListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
public class QwenChatModelProvider extends AbstractModelProvider implements AiRequestConvert<AiChatRequest, QwenChatRequest>,
        AiResponseConvert<QwenChatResponse, AiChatResponse> {

    public QwenChatModelProvider(AiConfiguration configuration) {
        super(configuration,
                Arrays.asList(
                        QwenEnum.Model.ALI_QWEN_TURBO.getCode(),
                        QwenEnum.Model.ALI_QWEN_PLUS.getCode(),
                        QwenEnum.Model.ALI_QWEN_MAX.getCode()
                )
        );
    }

    @Override
    public void executeStream(AiRequest aiRequest, EventSourceListener listener) {
        // 将通用参数 转化为 具体模型参数
        QwenChatRequest qwenChatRequest = convertRequest((AiChatRequest) aiRequest);
        // 构建请求对象
        QwenConfig aliConfig = configuration.getQwen();
        Request request = new Request.Builder()
                .url(aliConfig.getChatApi())
                .header(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + aliConfig.getApikey())
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .post(RequestBody.Companion.create(JSON.toJSONString(qwenChatRequest), okhttp3.MediaType.parse(MediaType.APPLICATION_JSON_VALUE)))
                .build();
        // 实例化EventSource，注册EventSource监听器，包装外部监听器，对响应数据进行处理
        factory.newEventSource(request, wrapEventSourceListener(listener, (result) -> JSON.parseObject(result, QwenChatResponse.class)));
    }

    @Override
    public boolean supports(String model) {
        return supportModels.contains(model);
    }

    @Override
    public QwenChatRequest convertRequest(AiChatRequest source) {
        // 将消息转换aliMessage
        List<QwenMessage> qwenMessages = source.getMessages().stream()
                .map(message -> new QwenMessage().setRole(message.getRole())
                        .setName(message.getName()).setContent(message.getContent())
                )
                .collect(Collectors.toList());
        // 转换请求
        return new QwenChatRequest()
                .setModel(source.getModel())
                .setInput(new QwenChatRequest.Input().setMessages(qwenMessages));
    }

    @Override
    public AiResponse convertResponse(QwenChatResponse response) {
        if (V.isEmpty(response.getOutput())) {
            return null;
        }
        String finishReason = response.getOutput().getFinishReason();
        List<QwenChatResponse.AliChoice> choices = response.getOutput().getChoices();
        if (V.notEmpty(finishReason)) {
            return new AiChatResponse()
                    .setChoices(Arrays.asList(new AiChatResponse.AiChoice()
                            .setFinishReason(finishReason)
                            .setMessage(new AiMessage().setRole(AiEnum.Role.SYSTEM.getCode())
                                    .setContent(response.getOutput().getText()))
                    ));
        }
//       入参 result_format = message
        else if (V.notEmpty(choices)) {
            return new AiChatResponse()
                    .setChoices(Arrays.asList(new AiChatResponse.AiChoice()
                            .setFinishReason(choices.get(0).getFinishReason())
                            .setMessage(BeanUtils.convert(choices.get(0).getMessage(), AiMessage.class))
                    ));
        }
        return null;
    }
}
