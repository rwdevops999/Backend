package com.tutopedia.backend.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Setting;

@Repository
public interface SettingRepository extends CrudRepository<Setting, Long> {
//	@Query("select s from Setting s where s.key = :key")
	Setting findByKey(String key);
}
