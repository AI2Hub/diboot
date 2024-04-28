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
