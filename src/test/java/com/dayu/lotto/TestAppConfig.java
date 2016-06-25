package com.dayu.lotto;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@ComponentScan(basePackages={"com.dayu.lotto.mongodb","com.dayu.lotto.service.impl"})
@Import(value={TestMongoDBConfig.class})
@EnableAsync
@EnableScheduling
public class TestAppConfig {
}
