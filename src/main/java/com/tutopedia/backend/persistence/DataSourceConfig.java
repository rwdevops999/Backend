package com.tutopedia.backend.persistence;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

//@Configuration
public class DataSourceConfig {
/*	@Value("${db_user:postgres}")
	private String user;
	
	@Value("${db_password:admin}")
	private String password;
	
	@Value("${db_host:db}")
	private String host;
	
	@Value("${db_port:5432}")
	private String port;
	
	@Value("${db_database:tutopedia_db}")
	private String database;
	
	@Profile("test")
	@Bean
    public DataSource dataSource() {
		System.out.println("===== INIT TEST DB =====");
		
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:tutopedia_db;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS public");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");

        return dataSource;
    }
	
	@Profile("dev")
	@Bean
    public DataSource getDataSource() {
		System.out.println("===== INIT DEV DB =====");
		System.out.println("USER     : " + user);
		System.out.println("PASSWORD : " + password);
		System.out.println("HOST     : " + host);
		System.out.println("PORT     : " + port);
		System.out.println("DATABASE : " + database);

		String url="jdbc:postgresql://"+host+":"+port+"/"+database;
		System.out.println("URL      : " + url);
		
		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/tutopedia_db");
        dataSourceBuilder.username("postgres");
        dataSourceBuilder.password("admin");
        dataSourceBuilder.url(url);
        dataSourceBuilder.username(user);
        dataSourceBuilder.password(password);

        return dataSourceBuilder.build();
    }
*/
}
