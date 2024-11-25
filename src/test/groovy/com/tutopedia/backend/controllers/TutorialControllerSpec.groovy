package com.tutopedia.backend.controllers

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
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
		body.add("published", "false")
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
}
