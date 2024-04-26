package com.diboot.ai.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 通用枚举
 *
 * @author : uu
 * @version v3.4
 * @Date 2024/4/26
 */
public interface AiEnum {

    /**
     * 对话时模型角色
     */
    @Getter
    @AllArgsConstructor
    enum Role {
        // messages.role
        // 通义千问模型角色（TODO 其他模型，角色后续统一调整）
        SYSTEM("system", "表示系统级消息，用于指导模型按照预设的规范、角色或情境进行回应。是否使用system角色是可选的，如果使用则必须位于messages的最开始部分。"),
        USER("user", "表示用户消息。与assistant应交替出现在对话中，模拟实际对话流程。"),
        ASSISTANT("assistant", "表示模型的消息。与user应交替出现在对话中，模拟实际对话流程。"),
        TOOL("tool", "表示工具的消息");
        /**
         * 角色编码
         */
        private String code;

        /**
         * AI 模型描述
         */
        private String desc;
    }
}
