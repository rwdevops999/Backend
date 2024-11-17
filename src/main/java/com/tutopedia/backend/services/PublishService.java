package com.tutopedia.backend.services;

import java.util.List;
import java.util.stream.StreamSupport;

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
	
	private void unpublishFileByTutorial(Tutorial tutorial) {
		if (tutorial.isPublished()) {
			// UNPUBLISH FILE HERE FROM OCI
			
			TutorialFile file = fileRepository.findByTutorialId(tutorial.getId());
			if (file == null) {
				throw new TutorialFileNotFoundException();
			}

			Bucket bucket = bucketRepository.findById(file.getBucketid()).orElseThrow(BucketNotFoundException::new);
			bucket.setTutorials(bucket.getTutorials() - 1);
			
			file.setBucketid(null);
			tutorial.setPublished(false);
			
			tutorialRepository.save(tutorial);
			bucketRepository.save(bucket);
			fileRepository.save(file);
			
			System.out.println("UNPUBLSIHED: Tutorial = " + tutorial.getId() + " from Bucket = " + bucket.getName());
		}
	}
	
	public void publishFileByTutorialId(Long tutorialId) {
		Tutorial tutorial = tutorialRepository.findById(tutorialId).orElseThrow(TutorialNotFoundException::new);

		publishFileByTutorial(tutorial);
	}

	public void publishAllFiles() {
		Iterable<Tutorial> tutorials = tutorialRepository.findByPublished(false);
		
		StreamSupport.stream(tutorials.spliterator(), false).forEach(tutorial -> publishFileByTutorial(tutorial));
/*		for (Tutorial tutorial : tutorials) {
			publishFileByTutorial(tutorial);
		} */
	}

	public void unpublishTutorials(List<Tutorial> tutorials) {
		tutorials.stream().forEach(tutorial -> unpublishFileByTutorial(tutorial));
	}
}
