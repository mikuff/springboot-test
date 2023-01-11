package com.lwl.springboottest.customstater.test.stater;

import com.lwl.springboottest.customstater.core.OrderStater;
import com.lwl.springboottest.customstater.test.service.TestAutowireService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@OrderStater(order = 2, methods = {"test"})
@Slf4j
public class TestOneStater {

    @Autowired
    private TestAutowireService testAutowire1;

    public void test() {
        log.info("-- executor one custom stater");
        testAutowire1.test();
    }

}
