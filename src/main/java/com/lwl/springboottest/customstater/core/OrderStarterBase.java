package com.lwl.springboottest.customstater.core;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 前面通过 OrderStaterRegistrar 可以抛弃在Spring其他注解的情况下将对象手动注入到spring容器中
 * 然后通过反射执行注解定义的方法
 */
@Slf4j
@Configuration
public class OrderStarterBase implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 监听spring初始化事件, CAS保证仅执行一次，反射调用代理类
     */
    @EventListener(WebServerInitializedEvent.class)
    public void bind(WebServerInitializedEvent event) {
        if (!this.running.get()) {
            // 从spring容器中获取所有打上 OrderStater 注解的类
            Map<String, Object> orderStaterMap = applicationContext.getBeansWithAnnotation(OrderStater.class);
            try {
                if (orderStaterMap.isEmpty()) {
                    return;
                }
                // 按照注解的order排序，order值大的先执行
                TreeSet<OrderStaterExecutor> executorSet = new TreeSet<>();
                for (Object obj : orderStaterMap.values()) {
                    OrderStater anno = obj.getClass().getAnnotation(OrderStater.class);
                    String[] method = anno.methods();
                    int order = anno.order();
                    if (method.length == 0) {
                        continue;
                    }
                    executorSet.add(new OrderStaterExecutor(obj, Arrays.asList(method), order));
                }
                for (OrderStaterExecutor executor : executorSet) {
                    executor.executor();
                }
            } catch (Exception e) {
                log.error("Execution start method error: {}", e.getLocalizedMessage());
            } finally {
                this.running.compareAndSet(false, true);
            }
        }
    }

    /**
     * 注解执行对象
     */
    private class OrderStaterExecutor implements Comparable<OrderStaterExecutor> {

        // spring 代理对象
        private Object proxy;

        // 声明要执行的方法
        private List<String> methods;

        // 排序
        private Integer order;

        public OrderStaterExecutor(Object proxy, List<String> methods, int order) {
            this.proxy = proxy;
            this.methods = methods;
            this.order = order;
        }

        public Integer getOrder() {
            return order;
        }

        /**
         * 通过反射执行方法
         */
        public void executor() {
            Method[] allMethods = ReflectionUtils.getDeclaredMethods(this.proxy.getClass());
            for (Method method : allMethods) {
                boolean contains = methods.contains(method.getName());
                if (!contains) {
                    continue;
                }
                ReflectionUtils.invokeMethod(method, this.proxy);
            }
        }

        @Override
        public int compareTo(OrderStaterExecutor other) {
            return other.getOrder().compareTo(this.order);
        }
    }


}