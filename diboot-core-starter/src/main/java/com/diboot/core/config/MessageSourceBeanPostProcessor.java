package com.diboot.core.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

/**
 * 自动添加国际化文件
 *
 * @author : uu
 * @version : v3.4
 * @Date 2024/6/13
 */
public class MessageSourceBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ResourceBundleMessageSource) {
            ResourceBundleMessageSource messageSource = (ResourceBundleMessageSource) bean;
            Set<String> basenameSet = messageSource.getBasenameSet();
            basenameSet.addAll(Arrays.asList("messages", "core_messages", "file_messages", "iam_messages", "mobile_messages", "notification_messages", "tenant_messages", "scheduler_messages", "ai_messages"));
            messageSource.setBasenames(basenameSet.toArray(new String[]{}));
            return messageSource;
        }
        return bean;
    }
}
