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
	private TutorialFileRepository tutorialFileRepository;

	@Autowired
	private QueryService queryService;

	@Autowired
	private CommandService commandService;

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
	
	public void publishFile(Tutorial tutorial) {
		if (! tutorial.isPublished()) {
			// PUBLISH FILE HERE TO OCI
			
			Bucket bucket = queryService.findDefaultBucket().orElseThrow(BucketNotFoundException::new);
			
			TutorialFile file = queryService.findTutorialFileByTutorialId(tutorial.getId()).orElseThrow(TutorialFileNotFoundException::new);
			file.setBucketid(bucket.getId());
			commandService.saveTutorialFile(file);
			
			bucket.setTutorials(bucket.getTutorials());
			commandService.saveBucket(bucket);
			
			tutorial.setPublished(true);
			commandService.saveTutorial(tutorial);
		}
	}
	
	public void publishFile(Long tutorialId) {
		Tutorial tutorial = queryService.findTutorialById(tutorialId).orElseThrow(TutorialNotFoundException::new);

		publishFile(tutorial);
	}

	public void publishAllFiles() {
		Iterable<Tutorial> tutorials = queryService.findTutorialsByPublishedFlag(false);
		for (Tutorial tutorial : tutorials) {
			publishFile(tutorial);
		}
	}
}
