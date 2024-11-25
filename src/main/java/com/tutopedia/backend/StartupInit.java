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
	
	@PostConstruct
	public void init() {
		try {
			osService.initialize();
		} catch (Exception e) {
			System.out.println("INIT EXCEPTION: " + e.getMessage());
		}
	}
}
