package com.xinxing.spring.framework.context;

import com.sun.istack.internal.Nullable;
import com.xinxing.spring.framework.beans.XBeanWrapper;
import com.xinxing.spring.framework.beans.annotation.XAutowired;
import com.xinxing.spring.framework.beans.factory.XBeanFactory;
import com.xinxing.spring.framework.beans.factory.config.XBeanDefinition;
import com.xinxing.spring.framework.beans.factory.support.XBeanDefinitionReader;
import com.xinxing.spring.framework.beans.factory.support.XDefaultListableBeanFactory;
import com.xinxing.spring.framework.stereotype.XController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
@Slf4j
public class XApplicationContext extends XDefaultListableBeanFactory implements XBeanFactory {

    private String[] locations;
    /** Cache of singleton objects created by FactoryBeans: FactoryBean name to object. */
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);
    /** Cache of unfinished FactoryBean instances: FactoryBean name to BeanWrapper. */
    private final ConcurrentMap<String, XBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    final String SCOPE_SINGLETON = "singleton";
    final String SCOPE_PROTOTYPE = "prototype";
    final String SCOPE_REQUEST = "request";
    final String SCOPE_SESSION = "session";
    final String SCOPE_APPLICATION = "application";

    public XApplicationContext(String... locations) {
        this.locations = locations;
        try {
            refresh();
        } catch (Exception e) {
            log.error("spring启动失败！！！");
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception{
        //1. 定位配置文件
        XBeanDefinitionReader reader = new XBeanDefinitionReader(locations);
        //2. 加载配置文件，扫描相关的类，把它们封装成 BeanDefinition
        List<XBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //3. 注册，把配置信息放到容器中 beanDefinitionMap
        doRegisterBeanDefinition(beanDefinitions);
        //4. 初始化，把 lazyInit 的类进行初始化
        doAutowired();

        log.debug("spring ioc 初始化完成 !!!");
    }

    private void doRegisterBeanDefinition(List<XBeanDefinition> beanDefinitions) throws Exception {
        beanDefinitions.forEach(xBeanDefinition -> {
            String beanName = xBeanDefinition.getFactoryBeanName();
            if (beanDefinitionMap.containsKey(beanName)) {
                XBeanDefinition existXBeanDefinition = beanDefinitionMap.get(beanName);
                throw new RuntimeException("存在相同的 beanName >>>> “" + beanName + "”, eg:[" + xBeanDefinition.getBeanClassName() + ", " + existXBeanDefinition.getBeanClassName() + "]");
            }
            registerBeanDefinition(beanName, xBeanDefinition);
        });
    }

    private void doAutowired() {
        beanDefinitionMap.forEach((beanName, xBeanDefinition) ->{
            if (!xBeanDefinition.isLazyInit()){//默认都是false
                getBean(beanName);
            }
        });
    }

    @Override
    public Object getBean(String beanName) {
        XBeanDefinition xBeanDefinition = beanDefinitionMap.get(beanName);
        Object instance = instantiateBean(beanName, xBeanDefinition);

        XBeanWrapper xBeanWrapper = new XBeanWrapper(instance);
        //存入IOC容器
        factoryBeanInstanceCache.put(beanName, xBeanWrapper);
        factoryBeanInstanceCache.put(xBeanDefinition.getBeanClassName(), xBeanWrapper);
        //依赖注入
        populateBean(beanName, xBeanWrapper);

        return factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    protected Object instantiateBean(final String beanName, final XBeanDefinition xBeanDefinition) {
        try {
            String beanClassName = xBeanDefinition.getBeanClassName();
            //默认都是单例的
            if (factoryBeanObjectCache.containsKey(beanName)) {
                return factoryBeanObjectCache.get(beanName);
            }
            Object instance = Class.forName(beanClassName).newInstance();
            //存入单例缓存器中
            factoryBeanObjectCache.put(beanName, instance);
            factoryBeanObjectCache.put(xBeanDefinition.getBeanClassName(), instance);
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("初始化[" + xBeanDefinition.getBeanClassName() + "]失败.");
        }
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values
     * from the bean definition.
     * @param beanName the name of the bean
     * @param xBeanWrapper the BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, @Nullable XBeanWrapper xBeanWrapper){
        Object wrappedInstance = xBeanWrapper.getWrappedInstance();
        Class<?> wrappedClass = xBeanWrapper.getWrappedClass();
        //只有@Controller的类才DI
        if(!(wrappedClass.isAnnotationPresent(XController.class)) ){
            return;
        }
        Field[] fields = wrappedClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(XAutowired.class)) {
                continue;
            }
            String xAutowiredBeanName = field.getAnnotation(XAutowired.class).value();
            if (StringUtils.isEmpty(xAutowiredBeanName)) {
                xAutowiredBeanName = field.getType().getName();
            }
            if (!factoryBeanInstanceCache.containsKey(xAutowiredBeanName)){
                throw new RuntimeException("DI失败，不存在 beanName:" + xAutowiredBeanName);
            }
            field.setAccessible(true);
            Object autowiredFieldInstance = factoryBeanInstanceCache.get(xAutowiredBeanName).getWrappedInstance();
            try {
                field.set(wrappedInstance, autowiredFieldInstance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
