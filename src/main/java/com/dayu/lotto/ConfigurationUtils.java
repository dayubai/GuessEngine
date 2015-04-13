package com.dayu.lotto;

import java.util.Properties;

public class ConfigurationUtils {

	public final static String salt = "bugaosuni";
	
	public static Properties getHibernateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
		return properties;
	}
	
	/*public static String[] getMappingResources() {
		return null;
	}*/
	
	public static Class<?>[] getAnnotatedClasses() {
		//return new Class[]{com.dayu.monitor.entity.User.class, com.dayu.monitor.entity.CurrencyRate.class};
		return null;
	}
}
