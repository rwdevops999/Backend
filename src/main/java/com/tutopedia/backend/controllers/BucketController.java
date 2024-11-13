package com.tutopedia.backend.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

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

import com.tutopedia.backend.error.BucketDuplicateException;
import com.tutopedia.backend.error.BucketNotFoundException;
import com.tutopedia.backend.persistence.model.Bucket;
import com.tutopedia.backend.services.CommandService;
import com.tutopedia.backend.services.QueryService;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping(path = "/api/bucket")
@CrossOrigin(origins = {"http://localhost:5173", "*"})
public class BucketController {
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
	public Iterable<Bucket> findAllBuckets() {
		log("findAllBuckets");
		
		return queryService.findAllBuckets();
	}

	@GetMapping("/find/{id}")
    @ResponseStatus(HttpStatus.OK)
	public Bucket findBucketById(@PathVariable(name = "id") @NotNull Long id) {
		log("findBucketById: " + id);
		
		return queryService.findBucketById(id).orElseThrow(BucketNotFoundException::new);
	}

	// FIND
	@GetMapping("/default")
    @ResponseStatus(HttpStatus.OK)
	public Bucket findDefaultBucket() {
		log("findDefaultBucket");
		
		return queryService.findDefaultBucket().orElseThrow(BucketNotFoundException::new);
	}

	// UPDATE
	@PutMapping("/default/{id}")
    @ResponseStatus(HttpStatus.OK)
	public void updateDefaultBucket(@PathVariable(name = "id") @NotNull Long id) {
		log("updateDefaultBucket");
		
		queryService.findBucketById(id).orElseThrow(BucketNotFoundException::new);
		
		commandService.updateDefaultBucketId(id);
	}
	
	// CREATE
	@PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
		public Bucket createBucket(@ModelAttribute @NotNull Bucket bucket) {
		log("createBucket");
		
		queryService.findBucketByName(bucket.getName()).ifPresent(s -> {
            throw new BucketDuplicateException();
        });
		
		return commandService.createBucket(bucket);
	}

	@DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
	public void deleteBucketById(@PathVariable(name="id") @NotNull Long id) {
		log("deleteBucket: " + id);
    	
		queryService.findBucketById(id).orElseThrow(BucketNotFoundException::new);
		
		commandService.deleteBucketById(id);
    }

	@DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
	public void deleteBuckets() {
		log("deleteBuckets");
    	
		commandService.deleteAllBuckets();
    }
}
