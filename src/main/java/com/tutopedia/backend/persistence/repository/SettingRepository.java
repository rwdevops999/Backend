package com.tutopedia.backend.persistence.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Setting;

@Repository
public interface SettingRepository extends CrudRepository<Setting, Long> {
	Setting findByKey(String key);
	List<Setting> findByType(String type);
}