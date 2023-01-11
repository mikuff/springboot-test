package com.lwl.springboottest.customstater.test.stater;

import com.lwl.springboottest.customstater.core.OrderStater;
import com.lwl.springboottest.customstater.test.service.TestAutowireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@OrderStater(order = 1, methods = {"test"})
@Slf4j
public class TestTwoStater {

    @Autowired
    private TestAutowireService testAutowire2;

    public void test() {
        log.info("-- executor two custom stater");
        testAutowire2.test();
    }

}
