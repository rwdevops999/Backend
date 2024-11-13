package com.tutopedia.backend.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.tutopedia.backend.persistence.model.Tutorial;
import com.tutopedia.backend.services.CommandService;
import com.tutopedia.backend.services.FileStorageService;
import com.tutopedia.backend.services.QueryService;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping(path = "/api")
@CrossOrigin(origins = {"http://localhost:5173", "*"})
public class TutorialController {
	@Autowired
	private CommandService commandService;

	@Autowired
	private QueryService queryService;

	@Autowired
	private FileStorageService fileService;
	
	private void log(String command) {
		Date currentDate = new Date();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentDateTime = dateFormat.format(currentDate);

		System.out.println("[" + currentDateTime + " : " + command + "]");
	}

	// GREETINGS
	@GetMapping("/greetings")
    @ResponseStatus(HttpStatus.OK)
	public String greetings() {
		log("greetings");
		
		return ("Hello ... This is a springboot REST app");	
	}

	// FIND
	@GetMapping("/find")
    @ResponseStatus(HttpStatus.OK)
	public Iterable<Tutorial> findAllTutorials() {
		log("findAllTutorials");
		
		return queryService.findAllTutorials();
	}

	@GetMapping("/find/published")
    @ResponseStatus(HttpStatus.OK)
	public Iterable<Tutorial> findAllPublishedTutorials (@RequestParam(required = false) Boolean published) {
		log("Find All Tutorials: " + (published?"(PUBLISHED)":"(UNPUBLISHED)"));
		
		return queryService.findTutorialsByPublishedFlag(published);
	}

	@GetMapping("/find/keywords/{keywords}" )
    @ResponseStatus(HttpStatus.OK)
	public Iterable<Tutorial> findTutorialsByKeyword (@PathVariable List<String> keywords) {
		log("Find Tutorials By Keywords: " + keywords.toString());
		
		List<Tutorial> tutorials = new ArrayList<Tutorial>();
		
		for (String keyword : keywords) {
			Iterable<Tutorial> lst = queryService.findByTitleContaining(keyword);
			for (Tutorial tutorial : lst) {
				if (! tutorials.contains(tutorial)) {
					tutorials.add(tutorial);
				}
			}

			lst = queryService.findByDescriptionContaining(keyword);
			for (Tutorial tutorial : lst) {
				if (! tutorials.contains(tutorial)) {
					tutorials.add(tutorial);
				}
			}
		}

		log("Found " + tutorials.size() + " Tutorials containing those Keywords");

		return tutorials;
	}
		
	@GetMapping("/find/{id}")
    @ResponseStatus(HttpStatus.OK)
	public Iterable<Tutorial>findTutorialsById(@PathVariable(name = "id") @NotNull Long id) {
		log("findTutorialsById: " + id);
		
		List<Tutorial> list = new ArrayList<Tutorial>();
		list.add( queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new));
		
		return list;
	}
	
	// CREATE
	@PostMapping(path = "/create", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
	public Tutorial createTutorial(@ModelAttribute @NotNull TutorialWithFile tutorialWithFile) {
		log("createTutorial");

		String fileName = StringUtils.cleanPath(tutorialWithFile.getTutorialFile().getOriginalFilename());

		Tutorial tutorial = commandService.saveTutorial(new Tutorial(
			tutorialWithFile.getTitle(), 
			tutorialWithFile.getDescription(), 
        	tutorialWithFile.getPublished(),
        	fileName
        ));

		commandService.saveFile(tutorial.getId(), tutorialWithFile).orElseThrow(FilePersistException::new);

		return tutorial;
	}
	
	// UPDATE
/*	@PutMapping(path = "/update/{id}", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE })
	@ResponseStatus(HttpStatus.OK)
	public Tutorial updateTutorialById(@PathVariable(name = "id") @NotNull Long id, @RequestBody Tutorial tutorial) {
		log("updateTutorialById: " + id);
		
		if (tutorial.getId() != id) {
			throw new TutorialIdMismatchException();
		}
		
		queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);
		
		return commandService.saveTutorial(tutorial);
	}
*/
	
	@PutMapping("/update/{id}")
	@ResponseStatus(HttpStatus.OK)
	public Tutorial updateTutorialById(@PathVariable(name = "id") @NotNull Long id, @ModelAttribute @NotNull TutorialWithFile tutorialWithFile) {
		log("updateTutorialById: " + id);

		Tutorial tutorial = queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);

		tutorial.setTitle(tutorialWithFile.getTitle());
		tutorial.setDescription(tutorialWithFile.getDescription());

		if (tutorialWithFile.getTutorialFile() != null) {
			System.out.println("UPDATE FILE");
			tutorial.setFilename(StringUtils.cleanPath(tutorialWithFile.getTutorialFile().getOriginalFilename()));
			commandService.updateFile(id, tutorialWithFile).orElseThrow(FilePersistException::new);
		}
		
		return commandService.saveTutorial(tutorial);
	}

	// PUBLISH
	@PutMapping("/publish")
	@ResponseStatus(HttpStatus.OK)
	public void publishAllTutorials() {
		log("publishAllTutorials");
		
		fileService.publishAllFiles();
//		commandService.publishAllTutorials();
	}

	@PutMapping("/publish/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void publishTutorialById(@PathVariable(name = "id") @NotNull Long id) {
		log("publishTutorialById: " + id);

		fileService.publishFile(id);
//		queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);

//		commandService.publishTutorialById(id);
	}

    @PutMapping("/publish/ids")
    @ResponseStatus(HttpStatus.OK)
	public void publishTutorialsByIds(@RequestParam @NotNull Map<String, String> ids) {
		log("publishTutorialByIds: " + ids.values());
    	
		for (String sid : ids.values()) {
			Long id = Long.parseLong(sid);
			queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);
			commandService.publishTutorialById(id);
		}
    }

	// DELETE
    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
	public void deleteAllTutorials() {
		log("deleteAllTutorials");
    	
		commandService.deleteAllFiles();
		commandService.deleteAllTutorials();
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)                      
	public void deleteTutorialById(@PathVariable(name="id") @NotNull Long id) {
		log("deleteTutorialById: " + id);

		queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);

		commandService.deleteTutorialById(id);
		commandService.deleteFileByTutorialId(id);
    }

    @DeleteMapping("/delete/ids")                                                                                                                    
    @ResponseStatus(HttpStatus.OK)
	public void deleteTutorialsByIds(@RequestParam @NotNull Map<String,String> ids) {
		log("deleteTutorialsByIds: " + ids.values());
                          
		for (String sid : ids.values()) {
			Long id = Long.parseLong(sid);
			queryService.findTutorialById(id).orElseThrow(TutorialNotFoundException::new);
			commandService.deleteTutorialById(id);
			commandService.deleteFileByTutorialId(id);
		}
    }
    }
