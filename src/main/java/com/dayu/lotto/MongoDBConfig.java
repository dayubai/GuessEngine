package com.dayu.lotto;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import com.mongodb.MongoClient;

@Configuration
@EnableMongoAuditing
public class MongoDBConfig extends AbstractMongoConfiguration {
	
	@Bean
	public MongoClient mongo() throws UnknownHostException {
		return new MongoClient("localhost",27017);
	}
	
	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}

	@Override
	protected String getDatabaseName() {
		return "lotto";
	}
	
//	@Bean
//    public AuditorAware<AuditableUser> myAuditorProvider() {
//        return new AuditorAwareImpl();
//    }
}
