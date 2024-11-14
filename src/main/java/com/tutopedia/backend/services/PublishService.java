package com.tutopedia.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutopedia.backend.error.BucketNotFoundException;
import com.tutopedia.backend.error.TutorialFileNotFoundException;
import com.tutopedia.backend.error.TutorialNotFoundException;
import com.tutopedia.backend.persistence.model.Bucket;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.BucketRepository;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;
import com.tutopedia.backend.persistence.repository.TutorialRepository;

@Service
public class PublishService {

	@Autowired
	private TutorialRepository tutorialRepository;
	
	@Autowired
	private BucketRepository bucketRepository;

	@Autowired
	private TutorialFileRepository fileRepository;

	private void publishFileByTutorial(Tutorial tutorial) {
		if (! tutorial.isPublished()) {
			// PUBLISH FILE HERE TO OCI
			
			Bucket bucket = bucketRepository.findBySelected(true);
			if (bucket == null) {
				throw new BucketNotFoundException();
			}
			
			TutorialFile file = fileRepository.findByTutorialId(tutorial.getId());
			if (file == null) {
				throw new TutorialFileNotFoundException();
			}
			file.setBucketid(bucket.getId());
			
			fileRepository.save(file);
			
			bucket.setTutorials(bucket.getTutorials()+1);
			bucketRepository.save(bucket);
			
			tutorial.setPublished(true);
			tutorialRepository.save(tutorial);
			
			System.out.println("PUBLSIHED: Tutorial = " + tutorial.getId() + " to Bucket = " + bucket.getName());
		}
	}
	
	public void publishFileByTutorialId(Long tutorialId) {
		Tutorial tutorial = tutorialRepository.findById(tutorialId).orElseThrow(TutorialNotFoundException::new);

		publishFileByTutorial(tutorial);
	}

	public void publishAllFiles() {
		Iterable<Tutorial> tutorials = tutorialRepository.findByPublished(false);
		for (Tutorial tutorial : tutorials) {
			publishFileByTutorial(tutorial);
		}
	}
}
