package com.tutopedia.backend.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tutopedia.backend.error.BucketNotFoundException;
import com.tutopedia.backend.error.TutorialFileNotFoundException;
import com.tutopedia.backend.error.TutorialNotFoundException;
import com.tutopedia.backend.persistence.model.Bucket;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;

import jakarta.transaction.Transactional;

@Service
public class FileStorageService {
	@Autowired
	private TutorialFileRepository fileRepository;

	public TutorialFile saveTutorialFile(TutorialFile file) {
		return fileRepository.save(file);
	}

	public void deleteByTutorialId(Long tid) {
		TutorialFile fileDB = fileRepository.findByTutorialId(tid);
		if (fileDB != null) {
			fileRepository.deleteById(fileDB.getId());
		}
	}

	public void deleteAll() {
		fileRepository.deleteAll();
	}

	public Optional<TutorialFile> create(Long tid, MultipartFile file) {
		try {
			TutorialFile fileDB = new TutorialFile(tid, file.getContentType(), file.getBytes());

			return Optional.of(fileRepository.save(fileDB));
		} catch (IOException ioe) {
			return Optional.empty();
		}
	}
	
	@Transactional	// needed for LOB
	public Optional<TutorialFile> update(Long tid, MultipartFile file) {
		TutorialFile fileDB = fileRepository.findByTutorialId(tid);
	    
		try {
		    fileDB.setFileContent(file.getBytes());
	
		    return Optional.of(fileRepository.save(fileDB));
		} catch (IOException ioe) {
			return Optional.empty();
		}
	}
	
}
