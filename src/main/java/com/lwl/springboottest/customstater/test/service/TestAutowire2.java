package com.lwl.springboottest.customstater.test.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestAutowire2 implements TestAutowireService {

    @Override
    public void test() {
        log.info("-- TestAutowire2 executor by autowire");
    }
}
