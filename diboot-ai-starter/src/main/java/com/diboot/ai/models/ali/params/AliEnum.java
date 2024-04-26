package com.diboot.ai.models.ali.params;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author : uu
 * @version v3.4
 * @Date 2024/4/26
 */
public interface AliEnum {
    @Getter
    @AllArgsConstructor
    enum Model {
        // 通义千问模型 ：https://help.aliyun.com/zh/dashscope/developer-reference/model-introduction?spm=a2c4g.11186623.0.0.6e2a512086lFix
        ALI_QWEN_TURBO("qwen-turbo", "通义千问超大规模语言模型，支持中文、英文等不同语言输入。"),
        ALI_QWEN_PLUS("qwen-plus", "通义千问超大规模语言模型增强版，支持中文、英文等不同语言输入。");
        /**
         * AI 模型编码
         */
        private String code;

        /**
         * AI 模型描述
         */
        private String desc;
    }
}
