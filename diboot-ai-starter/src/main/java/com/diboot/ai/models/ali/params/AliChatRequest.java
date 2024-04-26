package com.diboot.ai.models.ali.params;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通义千问请求
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
@Getter@Setter@Accessors(chain = true)
public class AliChatRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 4779276706100882299L;

    /**
     * 指定用于对话的通义千问模型名:默认使用qwen-turbo
     */
    private String model = AliEnum.Model.ALI_QWEN_TURBO.getCode();

    /**
     * 输入模型的信息
     */
    private Input input;


    /**
     * 通义千问 输入模型的信息
     *
     * 暂时只考虑messages
     */
    @Getter@Setter@Accessors(chain = true)
    public static class Input {

        /**
         * 表示用户与模型的对话历史
         */
        private List<AliMessage> messages;

    }
}
