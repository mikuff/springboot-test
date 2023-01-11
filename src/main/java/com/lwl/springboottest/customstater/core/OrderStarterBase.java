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
     * @param event
     * @Author lwl
     * @Description 监听spring初始化事件, 通过CAS保证仅执行一次，通过反射调用代理类
     * @Date 2022/8/19 16:21
     * @Return void
     **/
    @EventListener(WebServerInitializedEvent.class)
    public void bind(WebServerInitializedEvent event) throws Exception {
        if (!this.running.get()) {
            Map<String, Object> orderStaterMap = applicationContext.getBeansWithAnnotation(OrderStater.class);
            try {
                if (orderStaterMap.isEmpty()) {
                    return;
                }
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
                e.printStackTrace();
                log.error("Execution start method error: {}", e.getLocalizedMessage());
            } finally {
                this.running.compareAndSet(false, true);
            }
        }
    }

    private class OrderStaterExecutor implements Comparable<OrderStaterExecutor> {
        private Object proxy;
        private List<String> methods;
        private Integer order;

        public OrderStaterExecutor(Object proxy, List<String> methods, int order) {
            this.proxy = proxy;
            this.methods = methods;
            this.order = order;
        }

        public Integer getOrder() {
            return order;
        }

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