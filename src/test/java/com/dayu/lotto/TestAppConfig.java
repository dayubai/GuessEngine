package com.dayu.lotto;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.context.annotation.Bean;
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
	@Bean
	public JavaSparkContext javaSparkContext() throws Exception {
		System.setProperty("hadoop.home.dir", getClass().getResource("/hadoop").getPath());
		//ctxtBuilder = new ContextBuilder(tempFolder);
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("junit");
		return new JavaSparkContext(conf); 
	}
}
