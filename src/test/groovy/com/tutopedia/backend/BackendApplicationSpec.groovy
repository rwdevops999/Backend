package com.tutopedia.backend

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import spock.lang.Specification

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationSpec extends Specification {
	def "load context" () {
		
	}
}
