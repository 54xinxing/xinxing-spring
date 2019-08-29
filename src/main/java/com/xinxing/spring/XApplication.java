package com.xinxing.spring;

import com.xinxing.spring.framework.context.XApplicationContext;

public class XApplication {

    public static void main(String[] args) {
        new XApplicationContext("classpath:application.properties");
    }
}
