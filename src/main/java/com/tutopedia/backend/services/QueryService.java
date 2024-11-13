package com.tutopedia.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tutopedia.backend.persistence.model.Bucket;
import com.tutopedia.backend.persistence.model.Setting;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.persistence.repository.BucketRepository;
import com.tutopedia.backend.persistence.repository.SettingRepository;
import com.tutopedia.backend.persistence.repository.TutorialFileRepository;
import com.tutopedia.backend.persistence.repository.TutorialRepository;
import java.lang.Iterable;
import java.util.Optional;

@Service
public class QueryService {
	@Autowired
	private TutorialRepository tutorialRepository;

	@Autowired
	private BucketRepository bucketRepository;

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

	public Iterable<Tutorial> findTutorialsByPublishedFlag(boolean isPublished) {
		return tutorialRepository.findByPublished(isPublished);
	}

	public Iterable<Tutorial> findByTitleContaining(String keyword) {
		return tutorialRepository.findByTitleContainingIgnoreCase(keyword);
	}

	public Iterable<Tutorial> findByDescriptionContaining(String keyword) {
		return tutorialRepository.findByDescriptionContainingIgnoreCase(keyword);
		
	}

	public Iterable<Bucket> findAllBuckets() {
		return bucketRepository.findAll();
	}

	public Optional<Bucket> findDefaultBucket() {
		Bucket bucket = bucketRepository.findBySelected(true);
		
		if (bucket != null) {
			return Optional.of(bucket);
		}
		
		return Optional.empty();
	}

	public Optional<Bucket> findBucketById(long id) {
		return bucketRepository.findById(id);
	}

	public Optional<Bucket> findBucketByName(String name) {
		Bucket bucket = bucketRepository.findByName(name);
		
		if (bucket != null) {
			return Optional.of(bucket);
		}
		
		return Optional.empty();
	}
	
	public Optional<Setting> findSettingByKey(String key) {
		Setting setting = settingRepository.findByKey(key);
		
		if (setting != null) {
			System.out.println("FIND SETTING OK => " + setting.getValue());
			return Optional.of(setting);
		}
		
		System.out.println("FIND SETTING NOK => NULL");
		return Optional.empty();
	}
	
	public Optional<TutorialFile> findTutorialFileByTutorialId(Long tutorialId) {
		TutorialFile file = fileRepository.findByTutorialId(tutorialId);

		if (file != null) {
			return Optional.of(file);
		}
		
		return Optional.empty();
	}
}
