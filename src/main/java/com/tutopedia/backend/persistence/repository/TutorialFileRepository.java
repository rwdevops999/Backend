package com.tutopedia.backend.persistence.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.TutorialFile;

@Repository
public interface TutorialFileRepository extends CrudRepository<TutorialFile, Long> {
	TutorialFile findByTutorialId(long tid);
	List<TutorialFile> findByBucketId(long bid);
}
