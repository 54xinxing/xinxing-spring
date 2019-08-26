package com.xinxing.spring.framework.beans.factory.config;

import lombok.Data;

@Data
public class XBeanDefinition {

    private String beanClassName;

    private boolean lazyInit;

    private String factoryBeanName;
}
