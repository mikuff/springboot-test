package com.lwl.springboottest.customstater.core;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * 将注解的类注入到spring容器中
 */
public class OrderStaterRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry, importBeanNameGenerator);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        OrderStaterClassPathBeanDefinitionScanner scanner = new OrderStaterClassPathBeanDefinitionScanner(registry, false);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.registerFilters();
        // 由于这里的扫描路径没有办法动态获取，因此干脆将 @Import(value = {OrderStaterRegistrar.class}) 放到启动类上
        scanner.doScan(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
    }


    private class OrderStaterClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
        public OrderStaterClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
            super(registry, useDefaultFilters);
        }

        protected void registerFilters() {
            addIncludeFilter(new AnnotationTypeFilter(OrderStater.class));
        }

        @Override
        protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
            return super.doScan(basePackages);
        }
    }

}
