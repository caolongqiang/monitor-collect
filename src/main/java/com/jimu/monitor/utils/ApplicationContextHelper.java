package com.jimu.monitor.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 该类提供了spring上下文的简洁封装，只需要以下两步就可以间单的使用其api:
 * <ol>
 *     <li>
 （1）静态导入：<code>import static com.qunar.vacation.c2b.util.spring.ApplicationContextHelper.*;</code>
 *     </li>
 *     <li>
 *      （2）使用：<code>MyService service = getBean(MyService.class)</code>
 *     </li>
 * </ol>
 * <p>注意，需要将其扫描到spring bean容器，或进行配置</p>
 *
 * 理论上新代码，不应该使用这个类了，应该放到spring上下文中进行管理
 *
 * @since 1.0.0
 * @author chengya.li
 * @author kris.zhang
 */
public class ApplicationContextHelper implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextHelper.applicationContext = applicationContext;
    }

    @Deprecated // use popBean
    public static Object getBean(String beanName) {
        return applicationContext.getBean(beanName);
    }

    @Deprecated //use popBean
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }

    public static <T> T popBean(Class<T> clazz) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(clazz);
    }

    public static <T> T popBean(String name, Class<T> clazz) {
        if(applicationContext == null) return null;
        return applicationContext.getBean(name, clazz);
    }

}
