package com.tutopedia.backend.controllers

import java.net.http.HttpClient
import java.net.http.HttpRequest

import org.apache.commons.lang.RandomStringUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.methods.RequestBuilder
import org.apache.http.client.utils.URIBuilder
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.FileBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tutopedia.backend.BackendApplication
import com.tutopedia.backend.persistence.model.Repository
import com.tutopedia.backend.persistence.model.Tutorial
import com.tutopedia.backend.services.QueryService
import com.tutopedia.backend.test.ClearContext
import com.tutopedia.backend.test.TutorialTest
import com.tutopedia.backend.util.ParamBuilder

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import groovyx.net.http.RESTClientGroovy
import liquibase.serializer.core.string.StringChangeLogSerializer
import spock.lang.Ignore
import spock.lang.Specification

import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.runner.RunWith

@RunWith(SpringRunner.class)
@SpringBootTest(
	  webEnvironment = WebEnvironment.DEFINED_PORT,
	  classes = BackendApplication.class)
@TestPropertySource(
	  locations = "classpath:application-test.properties")
@TutorialTest 
class RepositoryControllerSpec extends Specification {
	def API_URL
	
	def httpclient
	def entitybuilder

	private sendRequest(String method, String url, String path, Object body = null, params = null, pathVar = false) {
		url += path
		if (params != null) {
			url += (pathVar ? "/" : "?") + params
		}

		def builder = RequestBuilder.create(method).setUri(url)
		if (body != null) {
			StringEntity entity = new StringEntity(new Gson().toJson(body), "UTF-8")
			builder
				.setEntity(entity)
				.setHeader("Accept", "application/json")
		}
			
		HttpUriRequest request = builder
		.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;chartset=UTF-8")
		.build()
		
		HttpClientBuilder.create().build().withCloseable {httpClient ->
		  httpClient.execute(request).withCloseable {response ->
			  println (response.toString());
			  
			  return response.statusLine.statusCode;
		  }
	  }
	}

	private sendRequestMultipart(String method, String url, String path, Object body = null, params = null, pathVar = false) {
		url += path
		if (params != null) {
			url += (pathVar ? "/" : "?") + params
		}

		def builder = MultipartEntityBuilder
				.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
				
		if (body != null) {
			Repository repository = (Repository)body
			def multiPartHttpEntity = builder
				.addTextBody("name", repository.name)
				.addTextBody("selected", repository.isSelected().toString())
				.build();

			def request = new RequestBuilder(method, url)
					.setEntity(multiPartHttpEntity)
					.build();

			HttpClientBuilder.create().build().withCloseable {httpClient ->
				httpClient.execute(request).withCloseable {response ->
					println (response.toString());
			  
					return response.statusLine.statusCode;
				}
			}
		} else {
			def request = new RequestBuilder(method, url)
			.build();

			HttpClientBuilder.create().build().withCloseable {httpClient ->
				httpClient.execute(request).withCloseable {response ->
					println (response.toString());
	  
					return response.statusLine.statusCode;
				}
			}
		}
	}

	def setup() {
		Properties properties = new Properties()
		this.getClass().getResource( '/tutopedia.properties' ).withInputStream {
			properties.load(it)
		}
		
		def API_URI = properties."API_URI"
		def API_PORT = properties."API_PORT"
		def API_PATH = properties."API_PATH_REPOSITORY"

		API_URL = "$API_URI:$API_PORT$API_PATH"
		println "API = " + API_URL
		
		httpclient = HttpClients.createDefault()
		entitybuilder = MultipartEntityBuilder.create();
		entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
	}

	/**
	 * create a repository instance with 
	 * - random name
	 * - selected flag passed as parameter
	 * - favorite flag defautt false
	 * - tutorials value default 0
	 *
	 * @param id: optional id value
	 * @param name: optional name value	
	 * @param name: selected boolean value	
	 * @return repository
	 */
	private def createRepository(
		def id = null, 
		def name = null, 
		def selected = null ) {
		Repository repository = new Repository();
		
		if (id != null) {
			repository.id = id
		}
		
		repository.name = name
		if (name == null) {
			repository.name= RandomStringUtils.randomAlphabetic(10)
		}
		
		if (selected == null) {
			repository.selected = false
		} else {
			repository.selected = selected
		}

		repository.favorite = false;
		repository.tutorials = 0;
		repository.updateDate = new Date();
		
		return repository
	}

	/**
	 * Convert an input stream to a string
	 * 	
	 * @param is : the input stream
	 * @return a String
	 */
	private def convertStreamToString(InputStream is) {
		def reader = new BufferedReader(new InputStreamReader(is));
		def sb = new StringBuilder();
	
		def line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
		
	/**
	 * create a repository in database through API
	 * 	
	 * @return id of repository or 0L if not created
	 */
	private def createRepositoryAsId() {
		Repository repository = createRepository();
		
		entitybuilder.addTextBody("name", repository.name)
		entitybuilder.addTextBody("selected", repository.selected.toString())
		entitybuilder.addTextBody("favorite", "false")
		entitybuilder.addTextBody("tutorials", String.valueOf(repository.tutorials))
		
		def multiPartHttpEntity = entitybuilder.build();
		def reqbuilder = RequestBuilder.post("$API_URL/create");

		reqbuilder.setEntity(multiPartHttpEntity);
		HttpUriRequest multipartRequest = reqbuilder.build();
		def response = httpclient.execute(multipartRequest);

		def entity = response.getEntity();
		if (entity != null) {
			def instream = entity.getContent()
			def result = convertStreamToString(instream)
			def jsonObject = JsonParser.parseString(result).getAsJsonObject();
			return jsonObject.get("id").getAsLong()
		}
		
		return 0L
	}
	
	private def createRepositoryAsId(Repository repository) {
		entitybuilder.addTextBody("name", repository.name)
		entitybuilder.addTextBody("selected", repository.selected.toString())
		entitybuilder.addTextBody("favorite", "false")
		entitybuilder.addTextBody("tutorials", String.valueOf(repository.tutorials))
		
		def multiPartHttpEntity = entitybuilder.build();
		def reqbuilder = RequestBuilder.post("$API_URL/create");

		HttpUriRequest multipartRequest = reqbuilder.build();
		reqbuilder.setEntity(multiPartHttpEntity);
		def response = httpclient.execute(multipartRequest);

		def entity = response.getEntity();
		if (entity != null) {
			def instream = entity.getContent()
			def result = convertStreamToString(instream)
			def jsonObject = JsonParser.parseString(result).getAsJsonObject();
			return jsonObject.get("id").getAsLong()
		}
		
		return 0L
	}
	
	@Ignore
	def "when find all repositories then OK"() {
		when: "find all repositories through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find")

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

	@Ignore
	def "create 1 repository, when find all repositories then should retrieve that one"() {
		given: "create a repository"
		createRepositoryAsId();
		
		when: "find all repositories through API"
			RestTemplate restTemplate = new RestTemplate()
			List<Repository> repositories = restTemplate.getForObject("$API_URL/find", List.class)
			
		then: "list should contain 1 repository"
		repositories.size() == 1;
	}	

	@Ignore
	def "delete all repositories, when find all repositories then should retrieve none"() {
		given: "delete all repositories"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
		
		when: "find all repositories through API"
			List<Repository> repositories = restTemplate.getForObject("$API_URL/find", List.class)
			
		then: "list should contain no repository"
		repositories.size() == 0;
	}	

	@Ignore
	def "when find default repository then OK"() {
		given: "create default repositoryin db API"
		  def repository = createRepository(123, "defaultrepo", true)
		  def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", repository)
		  
		when: "find default repository through API"
		  status = sendRequest(Method.GET.name(), "$API_URL", "/default")

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

	@Ignore
	def "when find default repository then should be selected"() {
		given: "create default repository in db API"
		  def repository = createRepository(123, "defaultbucket", true)
		  def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", repository)
		  
		when: "find default repository through API"
			RestTemplate restTemplate = new RestTemplate()
			Repository defaultRepository = restTemplate.getForObject("$API_URL/default", Repository.class)

		then: "repository should be selected"
		  	defaultRepository.isSelected() == true
	}	

	@Ignore
	def "when find default repository then should none present when not created"() {
		given: "delete all repositories and create non-default repository in db API"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
			def repository = createRepositoy(123, "repository")
			def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", repository)
		  
		when: "find default repository through API"
			Repository defaultRepository = restTemplate.getForObject("$API_URL/default", Repository.class)

		then: "no repositoryt found"
			  defaultRepository == null
	}

	@Ignore
	def "when create repository then CREATED"() {
		given: "setup multipart data"
			def repository = createRepository();
	
		when: "create repository in database using API"
			def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", repository)
			
		then: "response status should be CREATED"
			status == HttpStatus.SC_CREATED
	}

	@Ignore
	def "when delete repository by id then repository is removed"() {
		given: "setup multipart data"
			def id = createRepositoryAsId()
			
		when: "delete repository in database using API"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete/"+id)

		then: "response status should be NOT FOUND"
			def status = sendRequest(Method.GET.name(), "$API_URL", "/find/"+id)
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when update default repository then SC_OK"() {
		given: "create repository"
			def id = createRepositoryAsId()
			
		when: "update default repository in database using API"
			def status = sendRequest(Method.PUT.name(), "$API_URL", "/default/"+id)

		then: "response status should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when update default repository then SC_NOT_FOUND"() {
		given: "delete all repositories"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
			
		when: "update default repository in database using API"
			def status = sendRequest(Method.PUT.name(), "$API_URL", "/default/"+123)

		then: "response status should be NOT FOUND"
			status == HttpStatus.SC_NOT_FOUND
	}

	def "when update default repository then old default repository is not default, new repository is default"() {
		given: "create default and non default repository"
			def defaultBucket = createRepository(null, "default", true)
			Long defaultId = createRepositoryAsId(defaultBucket)
			println ("DEFAULT ID = " + defaultId)
			def nonDefaultRepository = createRepository(null, "nondefault")
			Long nonDefaultId = createRepositoryAsId(nonDefaultRepository)
			println ("NON DEFAULT ID = " + nonDefaultId)
			
		when: "update non default repository to default"
			sendRequest(Method.PUT.name(), "$API_URL", "/default/" + nonDefaultId)
			
		then: "old default repository should be non-default"
			RestTemplate restTemplate = new RestTemplate()
			def repository = restTemplate.getForObject("$API_URL/find/"+defaultId, Repository.class);
			def newrepository = restTemplate.getForObject("$API_URL/find/"+nonDefaultId, Repository.class);
			repository.isSelected() == false
			newrepository.isSelected() == true;
	}
}
