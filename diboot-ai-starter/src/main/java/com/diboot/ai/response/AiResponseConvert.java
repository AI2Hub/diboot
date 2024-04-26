package com.diboot.ai.response;

/**
 * 响应转换器
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
public interface AiResponseConvert<S, R extends AiResponse> {

    AiResponse convert(S response);
}
