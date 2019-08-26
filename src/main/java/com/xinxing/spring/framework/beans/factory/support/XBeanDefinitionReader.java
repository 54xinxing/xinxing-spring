package com.xinxing.spring.framework.beans.factory.support;

import com.xinxing.spring.framework.beans.factory.config.XBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class XBeanDefinitionReader {

    private static final String SCAN_PACKAGE = "scanPackage";
    private static final String CLASS_SUFFIX = ".class";

    private List<String> scanPackageBeanClassNames = new ArrayList<String>();

    private Properties config = new Properties();

    public XBeanDefinitionReader(String... locations) {
        //加载 properties 配置
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:", ""));
        try {
            config.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //扫描 scanPackage包下的所有类名
        docScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void docScanner(String scanPackage){
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                docScanner(scanPackage + "." + file.getName());
            } else if (file.getName().endsWith(CLASS_SUFFIX)){
                String className = scanPackage + "." + file.getName().replace(CLASS_SUFFIX, "");
                scanPackageBeanClassNames.add(className);
            }
        }
    }

    public List<XBeanDefinition> loadBeanDefinitions() {
        List<XBeanDefinition> beanDefinitions = new ArrayList<XBeanDefinition>();
        scanPackageBeanClassNames.forEach(beanClassName ->{
            try {
                Class<?> clazz = Class.forName(beanClassName);
                if (clazz.isInterface()) {
                    throw new RuntimeException("暂不支持接口bean，class name >>>> " + beanClassName);
                }
                XBeanDefinition xBeanDefinition = new XBeanDefinition();
                xBeanDefinition.setBeanClassName(beanClassName);
                String factoryBeanName = firstCaseLower(clazz.getSimpleName());
                xBeanDefinition.setFactoryBeanName(factoryBeanName);

                beanDefinitions.add(xBeanDefinition);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        return beanDefinitions;
    }

    private String firstCaseLower(String simpleName) {
        char [] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
