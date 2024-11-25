package com.tutopedia.backend.controllers

import javax.ws.rs.NotFoundException

import org.junit.jupiter.api.Assertions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate

import com.tutopedia.backend.BackendApplication
import com.tutopedia.backend.persistence.data.TutorialWithFile
import com.tutopedia.backend.persistence.model.Tutorial
import com.tutopedia.backend.test.RestTemplateResponseErrorHandler
import com.tutopedia.backend.test.TutorialTest

import spock.lang.Ignore
import spock.lang.Specification

@SpringBootTest(
	webEnvironment = WebEnvironment.DEFINED_PORT,
	classes = BackendApplication.class)
@TestPropertySource(
	locations = "classpath:application-test.properties")
@TutorialTest
@ActiveProfiles("test")
class TutorialControllerSpec extends Specification {
	def API_URL

	def File fileCreate
	def File fileUpdate

	def setup() {
		Properties properties = new Properties()
		this.getClass().getResource( '/tutopedia.properties' ).withInputStream {
			properties.load(it)
		}
		
		def API_URI = properties."API_URI"
		def API_PORT = properties."API_PORT"
		def API_PATH = properties."API_PATH"

		API_URL = "$API_URI:$API_PORT$API_PATH"
		
		println "API_URL = + $API_URL"
		 
		def path = new File(".").absolutePath
		if (fileCreate == null) {		
			fileCreate = new File(path+"/testData/create.txt");
		}
		
		if (fileUpdate == null) {		
			fileUpdate = new File(path+"/testData/update.txt")
		}
	}

	private createTutorial(Tutorial tutorial) {
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
		body.add("title", tutorial.title)
		body.add("description", tutorial.description)
		body.add("published", tutorial.published)
		body.add("tutorialFile", new FileSystemResource(fileCreate.path))

		HttpEntity<Object> request = new HttpEntity<Object>(body, headers)
		Tutorial response = restTemplate.postForObject("$API_URL/create", request, Tutorial.class);

		return response
	}
		
	@Ignore
	def "when greetings then OK"() {
		when: "call greetings throught API"
			RestTemplate restTemplate = new RestTemplate()
			String greetings = restTemplate.getForObject("$API_URL/greetings", String.class)
				
		then: "greetings should be ok"
			greetings == "Hello ... This is a springboot REST app"
	}
	
	@Ignore
	def "when create a tutorial, the id is filled"() {
		when: "create multipart data"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = false
			
			def Tutorial dbTutorial = createTutorial(tutorial)
			
		then: "id should be filled in"
			dbTutorial.id != null
			dbTutorial.title == tutorial.title
	}

	@Ignore
	def "when find tutorials, a list of tutorials is returned"() {
		when: "create 2 db tutorials"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = false
			
			createTutorial(tutorial)

			Tutorial tutorial2 = new Tutorial();
			tutorial2.title = "Tutorial2"
			tutorial2.description = "Description2"
			tutorial2.published = false

			createTutorial(tutorial2);

			RestTemplate restTemplate = new RestTemplate()
			Iterable<Tutorial> tutorials = restTemplate.getForObject("$API_URL/find", Iterable.class)

		then: "id should be filled in"
			tutorials.size() == 2
	}

	@Ignore
	def "when find tutorial by id, and tutorial is not in db, status NOT FOUND should be given"() {
		when: "find tutorial with id not in db"
			RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new RestTemplateResponseErrorHandler()).build();			
			
		then: "status should be NOT FOUND"
			Iterable<Tutorial> response
			try {
				response = restTemplate.getForObject("$API_URL/find/123", Iterable.class)
			} catch(NotFoundException e) {
				e.message.contains("404");
			}
	}

	@Ignore
	def "when find tutorial by id, and tutorial is in db, the tutorial is returned"() {
		when: "find tutorial with id in db"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = false
			
			createTutorial(tutorial)

			RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new RestTemplateResponseErrorHandler()).build();			
			
		then: "status should be NOT FOUND"
			try {
				Iterable<Tutorial> response = restTemplate.getForObject("$API_URL/find/123", Iterable.class)
				response.size() == 1
			} catch(NotFoundException e) {
			}
	}

	@Ignore
	def "when find published tutorials, the published tutorials are returned"() {
		when: "find tutorial with id in db"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = true
			createTutorial(tutorial)

			Tutorial tutorial2 = new Tutorial();
			tutorial2.title = "Tutorial2"
			tutorial2.description = "Description2"
			tutorial2.published = false
			createTutorial(tutorial2)
			
			RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new RestTemplateResponseErrorHandler()).build();			
			Iterable<Tutorial> tutorials = restTemplate.getForObject("$API_URL/find/published?published=true", Iterable.class)
			
		then: "returned tutorials size = 1"
			tutorials.size() == 1
	}

	@Ignore
	def "when find unpublished tutorials, the unpublished tutorials are returned"() {
		when: "find tutorial with id in db"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = true
			createTutorial(tutorial)

			Tutorial tutorial2 = new Tutorial();
			tutorial2.title = "Tutorial2"
			tutorial2.description = "Description2"
			tutorial2.published = false
			createTutorial(tutorial2)
			
			RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new RestTemplateResponseErrorHandler()).build();			
			Iterable<Tutorial> tutorials = restTemplate.getForObject("$API_URL/find/published?published=false", Iterable.class)
			
		then: "returned tutorials size = 1"
			tutorials.size() == 1
	}

	def "when delete all tutorials, no tutorials are returned"() {
		when: "create 2 tutorials in db"
			Tutorial tutorial = new Tutorial();
			tutorial.title = "Tutorial1"
			tutorial.description = "Description1"
			tutorial.published = true
			createTutorial(tutorial)

			Tutorial tutorial2 = new Tutorial();
			tutorial2.title = "Tutorial2"
			tutorial2.description = "Description2"
			tutorial2.published = false
			createTutorial(tutorial2)
			
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
			
			Iterable<Tutorial> tutorials = restTemplate.getForObject("$API_URL/find", Iterable.class)
			
		then: "returned tutorials size = 0"
			tutorials.size() == 0
	}
}
