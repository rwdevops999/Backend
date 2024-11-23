package com.tutopedia.backend.persistence.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface RepositoryRepository extends CrudRepository<com.tutopedia.backend.persistence.model.Repository, Long> {
	com.tutopedia.backend.persistence.model.Repository findBySelected(boolean isSelected);
	com.tutopedia.backend.persistence.model.Repository findByName(String name);

    @Modifying
	@Transactional
	@Query("update Repository r set r.selected = false where r.selected = true")
    void clearDefaultRepositories();

    @Modifying
	@Transactional
	@Query("update Repository r set r.selected = true where r.id = :id")
    void updateDefaultRepositoryId(@Param("id") Long id);
}
