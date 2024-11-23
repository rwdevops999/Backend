package com.tutopedia.backend.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tutopedia.backend.error.RepositoryDuplicateException;
import com.tutopedia.backend.error.RepositoryNotFoundException;
import com.tutopedia.backend.persistence.model.Repository;
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.persistence.model.TutorialFile;
import com.tutopedia.backend.services.CommandService;
import com.tutopedia.backend.services.QueryService;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/api/repository")
@CrossOrigin(origins = {"http://localhost:5173", "*"})
public class RepositoryController {
	@Autowired
	private CommandService commandService;

	@Autowired
	private QueryService queryService;

	private void log(String command) {
		Date currentDate = new Date();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentDateTime = dateFormat.format(currentDate);

		System.out.println("[" + currentDateTime + " : " + command + "]");
	}


	// FIND
	@GetMapping("/find")
    @ResponseStatus(HttpStatus.OK)
	public Iterable<Repository> findAllRepositories() {
		log("findAllRepositories");
		
		return queryService.findAllRepositories();
	}

	@GetMapping("/find/{id}")
    @ResponseStatus(HttpStatus.OK)
	public Repository findRepositoryById(@PathVariable(name = "id") @NotNull Long id) {
		log("findRepositoryById: " + id);
		
		return queryService.findRepositoryById(id).orElseThrow(RepositoryNotFoundException::new);
	}

	// FIND
	@GetMapping("/default")
    @ResponseStatus(HttpStatus.OK)
	public Repository findDefaultRepository() {
		log("findDefaultRepository");
		
		return queryService.findDefaultRepository().orElseThrow(RepositoryNotFoundException::new);
	}

	// UPDATE
	@PutMapping("/default/{id}")
    @ResponseStatus(HttpStatus.OK)
	public void updateDefaultRepository(@PathVariable(name = "id") @NotNull Long id) {
		log("updateDefaultRepository");
		
		queryService.findRepositoryById(id).orElseThrow(RepositoryNotFoundException::new);
		
		commandService.updateDefaultRepositoryId(id);
	}
	
	// CREATE
	@PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
		public Repository createRepository(@ModelAttribute @NotNull Repository repository) {
		log("createRepository");
		
		Optional<Repository> defaultRepository = queryService.findDefaultRepository();
		if (defaultRepository.isEmpty()) {
			repository.setSelected(true);
		}
		
		queryService.findRepositoryByName(repository.getName()).ifPresent(s -> {
            throw new RepositoryDuplicateException();
        });
		
		return commandService.createRepository(repository);
	}

	@DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
	public void deleteRepositoryById(@PathVariable(name="id") @NotNull Long id) {
		log("deleteRepository: " + id);
    	
		queryService.findRepositoryById(id).orElseThrow(RepositoryNotFoundException::new);
		
		commandService.deleteRepositoryById(id);
    }

	@DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
	public void deleteRepositories() {
		log("deleteRepositories");
    	
		commandService.deleteAllRepositories();
    }
	
	@GetMapping("/{repository}")
	@ResponseStatus(HttpStatus.OK)
	public List<Tutorial> getRepositoryTutorials(@PathVariable(name="repository") @NotNull String name) {
		System.out.println("getRepositoryTutorials: " + name);

		Repository repository = queryService.findRepositoryByName(name).orElseThrow(RepositoryNotFoundException::new);
		List<TutorialFile> files = queryService.findTutorialFilesByRepositoryId(repository.getId());
		List<Tutorial> tutorials = files.stream().map((TutorialFile file) -> (queryService.findTutorialByIdRaw(file.getTutorialId()))).toList();

		System.out.println("getRepositoryTutorials FOUND " + tutorials.size());

		return tutorials;
	}
}
