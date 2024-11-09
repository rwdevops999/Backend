package com.tutopedia.backend.persistence.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Bucket;

import jakarta.transaction.Transactional;

@Repository
public interface BucketRepository extends CrudRepository<Bucket, Long> {
	Bucket findBySelected(boolean isSelected);
	Bucket findByName(String name);

    @Modifying
	@Transactional
	@Query("update Bucket b set b.selected = false where b.selected = true")
    void clearDefaultBuckets();

    @Modifying
	@Transactional
	@Query("update Bucket b set b.selected = true where b.id = :id")
    void updateDefaultBucketId(@Param("id") Long id);
}
