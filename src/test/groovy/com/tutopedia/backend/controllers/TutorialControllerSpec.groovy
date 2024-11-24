package com.tutopedia.backend.controllers

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.RestTemplate

import com.tutopedia.backend.BackendApplication
import com.tutopedia.backend.test.TutorialTest

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
	}

	def "when greetings then OK"() {
		when: "call greetings throught API"
			RestTemplate restTemplate = new RestTemplate()
			String greetings = restTemplate.getForObject("$API_URL/greetings", String.class)
				
		then: "greetings should be ok"
			greetings == "Hello ... This is a springboot REST app"
	}
}
