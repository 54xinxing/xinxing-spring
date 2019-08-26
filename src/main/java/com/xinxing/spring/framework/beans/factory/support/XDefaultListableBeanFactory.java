package com.xinxing.spring.framework.beans.factory.support;

import com.xinxing.spring.framework.beans.factory.config.XBeanDefinition;
import com.xinxing.spring.framework.context.support.XAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class XDefaultListableBeanFactory extends XAbstractApplicationContext {

    /** Map of bean definition objects, keyed by bean name. */
    protected final Map<String, XBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);


    public void registerBeanDefinition(String beanName, XBeanDefinition xBeanDefinition) {
        beanDefinitionMap.put(beanName, xBeanDefinition);
    }

}
