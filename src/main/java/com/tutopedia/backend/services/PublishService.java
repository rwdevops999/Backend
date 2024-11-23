package com.tutopedia.backend.services;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutopedia.backend.error.RepositoryNotFoundException;
import com.tutopedia.backend.error.TutorialFileNotFoundException;
import com.tutopedia.backend.error.TutorialNotFoundException;
import com.tutopedia.backend.persistence.model.Repository;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.RepositoryRepository;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;
import com.tutopedia.backend.persistence.repository.TutorialRepository;

@Service
public class PublishService {

	@Autowired
	private TutorialRepository tutorialRepository;
	
	@Autowired
	private RepositoryRepository repoRepository;

	@Autowired
	private TutorialFileRepository fileRepository;

	private void publishFileByTutorial(Tutorial tutorial) {
		if (! tutorial.isPublished()) {
			// PUBLISH FILE HERE TO OCI
			
			Repository repository = repoRepository.findBySelected(true);
			if (repository == null) {
				throw new RepositoryNotFoundException();
			}
			
			TutorialFile file = fileRepository.findByTutorialId(tutorial.getId());
			if (file == null) {
				throw new TutorialFileNotFoundException();
			}
			file.setRepositoryid(repository.getId());
			
			fileRepository.save(file);
			
			repository.setTutorials(repository.getTutorials()+1);
			repoRepository.save(repository);
			
			tutorial.setPublished(true);
			tutorialRepository.save(tutorial);
			
			System.out.println("PUBLSIHED: Tutorial = " + tutorial.getId() + " to Repository = " + repository.getName());
		}
	}
	
	private void unpublishFileByTutorial(Tutorial tutorial) {
		if (tutorial.isPublished()) {
			// UNPUBLISH FILE HERE FROM OCI
			
			TutorialFile file = fileRepository.findByTutorialId(tutorial.getId());
			if (file == null) {
				throw new TutorialFileNotFoundException();
			}

			Repository repository = repoRepository.findById(file.getRepositoryid()).orElseThrow(RepositoryNotFoundException::new);
			repository.setTutorials(repository.getTutorials() - 1);
			
			file.setRepositoryid(null);
			tutorial.setPublished(false);
			
			tutorialRepository.save(tutorial);
			repoRepository.save(repository);
			fileRepository.save(file);
			
			System.out.println("UNPUBLSIHED: Tutorial = " + tutorial.getId() + " from Repository = " + repository.getName());
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
