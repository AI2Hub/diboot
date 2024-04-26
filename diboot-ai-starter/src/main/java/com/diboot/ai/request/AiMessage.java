package com.diboot.ai.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 请求消息体
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Getter
@Setter
@Accessors(chain = true)
public class AiMessage {
    // system、user、assistant和tool。
    private String role;

    private String content;
    // role为tool时不能省略
    private String name;
}
