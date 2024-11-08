package com.tutopedia.backend.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.TutorialFile;

@Repository
public interface TutorialFileRepository extends CrudRepository<TutorialFile, Long> {
	TutorialFile findByTutorialId(long tid);
}
