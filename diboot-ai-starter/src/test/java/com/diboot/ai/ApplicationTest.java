/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
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
package com.diboot.ai;

import com.diboot.ai.client.AiClient;
import com.diboot.ai.common.AiMessage;
import com.diboot.ai.common.request.AiChatRequest;
import com.diboot.ai.common.request.AiEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServletInitializer.class)
@Slf4j
public class ApplicationTest {

    @Autowired
    private AiClient aiClient;

    @Test
    public void testChat() throws Exception {
        AiChatRequest aiRequest = new AiChatRequest();
        aiRequest.setMessages(
                Arrays.asList(new AiMessage().setRole(AiEnum.Role.USER.getCode()).setContent("java 框架 diboot 简介"))
        );
//        aiClient.executeStream(aiRequest, new SseEmitter());
    }
}