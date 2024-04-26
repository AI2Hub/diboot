package com.diboot.ai.models;

import com.diboot.ai.config.AiConfiguration;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型抽象提供
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/4/25
 */
public abstract class AbstractModelProvider implements ModelProvider {

    // 当前拦截器支持的模型
    protected List<String> supportModels = new ArrayList<>();
    protected final AiConfiguration configuration;
    protected final EventSource.Factory factory;

    public AbstractModelProvider(AiConfiguration configuration, List<String> supportModels) {
        this.supportModels.addAll(supportModels);
        this.configuration = configuration;
        // 实例化EventSource工厂，使用 factory.newEventSource()创建EventSource，需要传递一个用于处理服务器发送事件的实例，并定义处理事件的回调逻辑
        this.factory = EventSources.createFactory(configuration.getOkhttpClient());
    }
}
