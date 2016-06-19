package com.jimu.monitor.collect.db;

import java.lang.annotation.*;

import org.springframework.stereotype.Component;

/**
 * 标识MyBatis的DAO,方便MapperScannerConfigurer的扫描。
 *
 * 使用以下例子：
 * <pre>
 *       <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
 *           <property name="basePackage" value="com.qunar.vacation.b2c"/>
 *           <property name="annotationClass" value="com.qunar.vacation.commons.web.util.MyBatisRepository"/>
 *       </bean>
 * </pre>
 *
 * @since 1.0.0
 * @author zhenbao.zhou
 * @author chengya.li
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface MyBatisRepository {
	String value() default "";
}
