package com.tutopedia.backend.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tutopedia.backend.error.FilePersistException;
import com.tutopedia.backend.error.TutorialNotFoundException;
import com.tutopedia.backend.persistence.data.TutorialWithFile;
import com.tutopedia.backend.persistence.model.Setting;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.services.CommandService;
import com.tutopedia.backend.services.QueryService;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping(path = "/api/settings")
@CrossOrigin(origins = {"http://localhost:5173", "*"})
public class SettingController {
	@Autowired
	private QueryService queryService;

	private void log(String command) {
		Date currentDate = new Date();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentDateTime = dateFormat.format(currentDate);

		System.out.println("[" + currentDateTime + " : " + command + "]");
	}

	@GetMapping("/{key}")
    @ResponseStatus(HttpStatus.OK)
	public Setting findSettingByKey(@PathVariable(name = "key") @NotNull String key) {
		log("findSettingByKey: " + key);

		Optional<Setting> setting = queryService.findSettingByKey(key);
	
		if (setting.isEmpty()) {
			return  null;
		}
		
		return setting.get();
	}
}
