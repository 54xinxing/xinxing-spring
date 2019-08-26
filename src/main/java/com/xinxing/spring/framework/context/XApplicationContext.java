package com.xinxing.spring.framework.context;

import com.sun.istack.internal.Nullable;
import com.xinxing.spring.framework.beans.XBeanWrapper;
import com.xinxing.spring.framework.beans.factory.XBeanFactory;
import com.xinxing.spring.framework.beans.factory.config.XBeanDefinition;
import com.xinxing.spring.framework.beans.factory.support.XBeanDefinitionReader;
import com.xinxing.spring.framework.beans.factory.support.XDefaultListableBeanFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class XApplicationContext extends XDefaultListableBeanFactory implements XBeanFactory {

    private String[] locations;
    /** Cache of singleton objects created by FactoryBeans: FactoryBean name to object. */
    private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<>(16);
    /** Cache of unfinished FactoryBean instances: FactoryBean name to BeanWrapper. */
    private final ConcurrentMap<String, XBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    public XApplicationContext(String... locations) {
        this.locations = locations;
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
    }

    private void doRegisterBeanDefinition(List<XBeanDefinition> beanDefinitions) throws Exception {
        beanDefinitions.forEach(xBeanDefinition -> {
            String beanName = xBeanDefinition.getFactoryBeanName();
            if (beanDefinitionMap.containsKey(beanName)) {
                XBeanDefinition existXBeanDefinition = beanDefinitionMap.get(beanName);
                throw new RuntimeException("存在相同的 BeanName >>>> “" + beanName + "”, eg:[" + xBeanDefinition.getBeanClassName() + ", " + existXBeanDefinition.getBeanClassName() + "]");
            }
            registerBeanDefinition(beanName, xBeanDefinition);
        });
    }

    private void doAutowired() {
        beanDefinitionMap.forEach((beanName, xBeanDefinition) ->{
            if (!xBeanDefinition.isLazyInit()){
                getBean(beanName);
            }
        });
    }

    @Override
    public Object getBean(String beanName) {
        XBeanDefinition xBeanDefinition = beanDefinitionMap.get(beanName);
        Object instance = instantiateBean(beanName, xBeanDefinition);
        return null;
    }

    protected XBeanWrapper instantiateBean(final String beanName, final XBeanDefinition xBeanDefinition){
        XBeanWrapper xBeanWrapper = new XBeanWrapper();

        return xBeanWrapper;
    }

    /**
     * Populate the bean instance in the given BeanWrapper with the property values
     * from the bean definition.
     * @param beanName the name of the bean
     * @param bw the BeanWrapper with bean instance
     */
    protected void populateBean(String beanName, @Nullable XBeanWrapper bw){

    }

}
