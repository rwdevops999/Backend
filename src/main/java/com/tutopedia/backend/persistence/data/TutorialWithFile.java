package com.tutopedia.backend.persistence.data;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TutorialWithFile {
	@NotEmpty
    private String title;

	@NotEmpty
    private String description;
	
	@NotEmpty
    private Boolean published;
    
	private MultipartFile tutorialFile;
}
