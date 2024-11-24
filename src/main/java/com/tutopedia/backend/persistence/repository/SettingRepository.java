package com.tutopedia.backend.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tutopedia.backend.persistence.model.Setting;

import jakarta.transaction.Transactional;

@Repository
public interface SettingRepository extends CrudRepository<Setting, Long> {
    @Modifying
	@Transactional
	@Query("select * from Settings s where s.key = :key and s.type = :type")
	Setting findByKeyAndType(String key, String type);
	List<Setting> findByType(String type);
}