package com.diboot.ai.request;

import com.diboot.ai.models.ali.params.AliEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 对话请求
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/26
 */
@Getter
@Setter
public class AiChatRequest implements AiRequest {


    /**
     * 对话消息
     */
    private List<AiMessage> messages;

    /**
     * 对话模型
     */
    String model = AliEnum.Model.ALI_QWEN_TURBO.getCode();
}
