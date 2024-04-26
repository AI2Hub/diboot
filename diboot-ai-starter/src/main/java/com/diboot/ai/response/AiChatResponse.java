
package com.diboot.ai.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对话请求响应
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
@Getter
@Setter
public class AiChatResponse implements AiResponse, Serializable {


    @Serial
    private static final long serialVersionUID = 1897111766782881992L;
}
