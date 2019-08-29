package com.xinxing.spring.framework.beans;

import lombok.Data;

@Data
public class XBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public XBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
        this.wrappedClass = wrappedInstance.getClass();
    }
}
