package com.tutopedia.backend.persistence.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Tutorial;

import jakarta.transaction.Transactional;

@Repository
public interface TutorialRepository extends CrudRepository<Tutorial, Long> {
	Iterable<Tutorial> findByPublished(boolean isPublished);
	Iterable<Tutorial> findByTitleContainingIgnoreCase(String keyword);
	Iterable<Tutorial> findByDescriptionContainingIgnoreCase(String keyword);
	
    @Modifying
	@Transactional
	@Query("update Tutorial t set t.published = true")
    void publishAll();

    @Modifying
	@Transactional
	@Query("update Tutorial t set t.published = true where t.id = :id")
    void publishById(@Param("id") Long id);
}
