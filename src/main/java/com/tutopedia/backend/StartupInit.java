package com.tutopedia.backend;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tutopedia.backend.services.oci.OsService;

@Component
public class StartupInit {
	@Autowired
	OsService osService;
	
	@Value("${spring.datasource.url}")
	private String db_datasource;

	@Value("${spring.datasource.username}")
	private String db_user;

	@Value("${spring.datasource.password}")
	private String db_password;
	
	private void showDBConnection() {
		System.out.println("===== DB =====");
		System.out.println("DATASOURCE: " + db_datasource);
		System.out.println("USER      : " + db_user);
		System.out.println("PASSWORD  : " + db_password);
	}
	
	@PostConstruct
	public void init() {
		try {
			showDBConnection();
			osService.initialize();
		} catch (Exception e) {
			System.out.println("INIT EXCEPTION: " + e.getMessage());
		}
	}
}
