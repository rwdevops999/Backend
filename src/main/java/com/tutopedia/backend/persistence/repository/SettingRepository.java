package com.tutopedia.backend.persistence.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Setting;

@Repository
public interface SettingRepository extends CrudRepository<Setting, Long> {
	Setting findByKeyAndType(String key, String Type);
	List<Setting> findByType(String type);
}