package com.dayu.lotto;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(basePackages={"com.dayu.lotto"})
@Import(value={MongoDBConfig.class})
@ImportResource({"/WEB-INF/lotto-servlet.xml"})
@EnableAsync
@EnableScheduling
@EnableWebMvc
public class AppConfig {	
	
}
