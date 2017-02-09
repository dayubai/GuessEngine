package com.dayu.lotto;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@ComponentScan(basePackages={"com.dayu.lotto"})
@Import(value={MongoDBConfig.class})
@ImportResource({"/WEB-INF/lotto-servlet.xml"})
@EnableAsync
@EnableScheduling
@EnableWebMvc
public class AppConfig {	
	@Bean
	public JavaSparkContext javaSparkContext() throws Exception {
		System.setProperty("hadoop.home.dir", getClass().getResource("/hadoop").getPath());
		//ctxtBuilder = new ContextBuilder(tempFolder);
		SparkConf conf = new SparkConf();
		conf.setMaster("local[2]");
		conf.setAppName("junit");
		return new JavaSparkContext(conf); 
	}
	
	@Bean(name = "multipartResolver")
	public CommonsMultipartResolver commonsMultipartResolver(){
	    CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
	    commonsMultipartResolver.setDefaultEncoding("utf-8");
	    commonsMultipartResolver.setMaxUploadSize(50000000);
	    return commonsMultipartResolver;
	}
}
