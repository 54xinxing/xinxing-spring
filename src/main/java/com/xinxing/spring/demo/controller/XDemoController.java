package com.xinxing.spring.demo.controller;

import com.xinxing.spring.demo.service.XDemoService;
import com.xinxing.spring.framework.beans.annotation.XAutowired;
import com.xinxing.spring.framework.stereotype.XController;

@XController
public class XDemoController {

    @XAutowired
    private XDemoService xDemoService;

}
