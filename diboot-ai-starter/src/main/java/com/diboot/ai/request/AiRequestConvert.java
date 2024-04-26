package com.diboot.ai.request;

/**
 * 请求转换器
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
public interface AiRequestConvert<S extends AiRequest, R> {

    R convert(S aiRequest);
}
