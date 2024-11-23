package com.tutopedia.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutopedia.backend.persistence.model.Repository;
import com.tutopedia.backend.persistence.model.Setting;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.RepositoryRepository;
import com.tutopedia.backend.persistence.repository.SettingRepository;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;
import com.tutopedia.backend.persistence.repository.TutorialRepository;
import java.lang.Iterable;
import java.util.List;
import java.util.Optional;

@Service
public class QueryService {
	@Autowired
	private TutorialRepository tutorialRepository;

	@Autowired
	private RepositoryRepository repoRepository;

	@Autowired
	private SettingRepository settingRepository;
	
	@Autowired
	private TutorialFileRepository fileRepository;
	
	public Iterable<Tutorial> findAllTutorials() {
		return tutorialRepository.findAll();
	}
	
	public Optional<Tutorial> findTutorialById(long id) {
		return tutorialRepository.findById(id);
	}

	public Tutorial findTutorialByIdRaw(long id) {
		Optional<Tutorial> tutorial = tutorialRepository.findById(id);
		
		if (tutorial.isPresent()) {
			return tutorial.get();
		}
		
		return null;
	}

	public Iterable<Tutorial> findTutorialsByPublishedFlag(boolean isPublished) {
		return tutorialRepository.findByPublished(isPublished);
	}

	public Iterable<Tutorial> findByTitleContaining(String keyword) {
		return tutorialRepository.findByTitleContainingIgnoreCase(keyword);
	}

	public Iterable<Tutorial> findByDescriptionContaining(String keyword) {
		return tutorialRepository.findByDescriptionContainingIgnoreCase(keyword);
		
	}

	public Iterable<Repository> findAllRepositories() {
		return repoRepository.findAll();
	}

	public Optional<Repository> findDefaultRepository() {
		Repository repository = repoRepository.findBySelected(true);
		
		if (repository != null) {
			return Optional.of(repository);
		}
		
		return Optional.empty();
	}

	public Optional<Repository> findRepositoryById(long id) {
		return repoRepository.findById(id);
	}

	public Optional<Repository> findRepositoryByName(String name) {
		Repository repository = repoRepository.findByName(name);
		
		if (repository != null) {
			return Optional.of(repository);
		}
		
		return Optional.empty();
	}
	
	public Optional<Setting> findSettingByKeyAndType(String key, String type) {
		Setting setting = settingRepository.findByKeyAndType(key, type);
		
		if (setting != null) {
			System.out.println("FIND SETTING OK => " + setting.getValue());
			return Optional.of(setting);
		}
		
		System.out.println("FIND SETTING NOK => NULL");
		return Optional.empty();
	}
	
	public List<Setting> findSettingsByType(String type) {
		List<Setting> settings = settingRepository.findByType(type);
		
		return settings;
	}
	
	public Optional<TutorialFile> findTutorialFileByTutorialId(Long tutorialId) {
		TutorialFile file = fileRepository.findByTutorialId(tutorialId);

		if (file != null) {
			return Optional.of(file);
		}
		
		return Optional.empty();
	}

	public List<TutorialFile> findTutorialFilesByRepositoryId(Long bucketId) {
		return fileRepository.findByRepositoryid(bucketId);
	}
}

