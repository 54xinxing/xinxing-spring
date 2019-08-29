package com.xinxing.spring.demo.service;

public interface XDemoService {

    String query(String name);

    String add(String name, String addr) throws Exception;

    String edit(Integer id, String name, String addr);

    String remove(Integer id);
}
