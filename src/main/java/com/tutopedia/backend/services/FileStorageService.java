package com.tutopedia.backend.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;

import jakarta.transaction.Transactional;

@Service
public class FileStorageService {
	@Autowired
	private TutorialFileRepository tutorialFileRepository;
	
	public Optional<TutorialFile> store(Long tid, MultipartFile file) {
		try {
			TutorialFile fileDB = new TutorialFile(tid, file.getContentType(), file.getBytes());

			return Optional.of(tutorialFileRepository.save(fileDB));
		} catch (IOException ioe) {
			return Optional.empty();
		}
	}
	
	@Transactional	// needed for LOB
	public Optional<TutorialFile> update(Long tid, MultipartFile file) {
		TutorialFile fileDB = tutorialFileRepository.findByTutorialId(tid);
	    
		try {
		    fileDB.setFileContent(file.getBytes());
	
		    return Optional.of(tutorialFileRepository.save(fileDB));
		} catch (IOException ioe) {
			return Optional.empty();
		}
	}
	
	public void deleteAll() {
		tutorialFileRepository.deleteAll();
	}
	
	public void deleteFileByTutorialId(Long tid) {
		TutorialFile fileDB = tutorialFileRepository.findByTutorialId(tid);
		if (fileDB != null) {
			tutorialFileRepository.deleteById(fileDB.getId());
		}
	}
}
