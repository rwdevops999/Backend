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
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.TestPropertySource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tutopedia.backend.BackendApplication
import com.tutopedia.backend.persistence.model.Bucket
import com.tutopedia.backend.persistence.model.Tutorial
import com.tutopedia.backend.test.ClearContext
import com.tutopedia.backend.test.TutorialTest
import com.tutopedia.backend.util.ParamBuilder

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClientGroovy
import liquibase.serializer.core.string.StringChangeLogSerializer
import spock.lang.Ignore
import spock.lang.Specification

import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients

@SpringBootTest(
	  webEnvironment = WebEnvironment.DEFINED_PORT,
	  classes = BackendApplication.class)
@TestPropertySource(
	  locations = "classpath:application-test.properties")
@TutorialTest
class TutorialControllerSpec extends Specification {
	
	def API_URL
	
	def fileCreate
	def fileUpdate

	def httpclient
	def entitybuilder

	private buildMultiParam(Map<String,Object> params) {
		return ParamBuilder.build(params)
	}
	
	/**
	 * Send request
	 * 	
	 * @param url: the REST end-point
	 * @param method: GET/PUT/POST/DELETE
	 * @param params: RequestParams (PathVariable or RequestParams)
	 * @param pathVar: true if params contains a path variable (false for RequestParams)
	 * @return status code (int)
	 */
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
			Tutorial tutorial = (Tutorial)body
			def multiPartHttpEntity = builder
				.addTextBody("title", tutorial.title)
				.addTextBody("description", tutorial.description)
				.addTextBody("published", "false")
				.addBinaryBody("tutorialFile", (method == "POST" ? fileCreate : fileUpdate))
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
		} 	
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
		
	def setup() {
		Properties properties = new Properties()
		this.getClass().getResource( '/tutopedia.properties' ).withInputStream {
			properties.load(it)
		}
		
		def API_URI = properties."API_URI"
		def API_PORT = properties."API_PORT"
		def API_PATH = properties."API_PATH"

		API_URL = "$API_URI:$API_PORT$API_PATH"
		
		def currentDirFile = new File(".")
		def helper = currentDirFile.absolutePath
		if (fileCreate == null) {		
			fileCreate = new File(helper+"/testData/create.txt")
		}
		
		if (fileUpdate == null) {		
			fileUpdate = new File(helper+"/testData/update.txt")
		}
		
		httpclient = HttpClients.createDefault()
		entitybuilder = MultipartEntityBuilder.create();
		entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
	}

	/**
	 * create a tutorial instance with 
	 * - random title
	 * - random description
	 * - published flag false
	 *
	 * @param id: optional id value
	 * @param title: optional title value	
	 * @return tutorial
	 */
	private def createTutorial(def id = null, def title = null, def published = null) {
		Tutorial tutorial = new Tutorial();
		
		if (id != null) {
			tutorial.id = id
		}
		
		tutorial.title = title
		if (title == null) {
			tutorial.title = RandomStringUtils.randomAlphabetic(10)
		}
		tutorial.description = RandomStringUtils.randomAlphabetic(15)
		
		if (published == null) {
			tutorial.published = false
		} else {
			tutorial.published = true
		}

		return tutorial
	}

	/**
	 * create a tutorial in database through API
	 * 	
	 * @return id of tutorial or 0L if not created
	 */
	private def createTutorialAsId() {
		Tutorial tutorial = createTutorial();
		
		entitybuilder.addTextBody("title", tutorial.title)
		entitybuilder.addTextBody("description", tutorial.description)
		entitybuilder.addTextBody("published", "false")
		entitybuilder.addBinaryBody("tutorialFile", fileCreate)
		
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

	@Ignore
	def "when greetings then OK"() {
		when: "call greetings throught API"
			def status = sendRequest(Method.GET.name(), "$API_URL", "/greetings")
				
		then: "response status should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when create tutorial then CREATED"() {
		given: "setup multipart data"
			def tutorial = createTutorial();
	
		when: "create tutorial in database using API"
			def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", tutorial)
			
		then: "response status should be CREATED"
			status == HttpStatus.SC_CREATED
	}
		
	@Ignore
	def "when find all tutorials then OK"() {
		when: "find all tutorials through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find")

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

	@Ignore
	def "when find all unpublished tutorials then OK"() {
		given: "create unpublished tutorial"
		  createTutorial();
		  
		when: "find all published tutorials through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find/published", null, buildMultiParam(["published": false]) )

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

/*	def "when find all published tutorials then OK"() {
		given: "create published tutoiral"
		  createTutorial(null, null, true);
		  
		when: "find all published tutorials through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find/published", null, buildMultiParam(["published": true]) )

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	
*/
	@Ignore
	def "when find tutorial by id then OK"() {
		given: "create tutorial in database"
  		  def id = createTutorialAsId();
		
		when: "find tutorial by id through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find", null, id, true)

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when find tutorial by id then NOT FOUND"() {
		given: "create tutorial in database"
		  def id = createTutorialAsId();
		
		when: "find tutorials by id which is not in the database"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find", null, id+1, true)

		then: "response status should be NOT FOUND"
		  	status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when update tutorial by id then OK"() {
		given: "create tutorial in database and new tutorial instance"
  		  def id = createTutorialAsId();
		  def tutorial = createTutorial(id, "updateTitle");
	
		when: "update tutorial throught API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/update", tutorial, id, true)
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@ClearContext
	@Ignore
	def "when update tutorial by mismatch id then BAD REQUEST"() {
		given: "create tutorial in datase and tutorial instance with mismatching id"
  		  def id = createTutorialAsId()
		  def tutorial = createTutorial(id + 1, "updateTitle")
	
		when: "update tutorial with tutorial instance and id"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/update", tutorial, id, true)
  
		then: "response should be BAD REQUEST"
		  	status == HttpStatus.SC_BAD_REQUEST
	}

	@Ignore
	def "when update tutorial by not existing id then NOT FOUND"() {
		given: "create no tutorial in database, just a tutorial instance"
			def id = 1
			def tutorial = createTutorial(id);
		when: "update the tutorial (id is not in database)"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/update", tutorial, id, true)
  
		then: "result should be NOT FOUND"
		  	status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when update tutorial by id and with file then OK"() {
		given: "create a tutorial in database"
			def id = createTutorialAsId()
			def tutorial = createTutorial()
	
		when: "update the tutorial throught API"
		  def status = sendRequestMultipart(Method.PUT.name(), "$API_URL", "/update/file", tutorial, id, true)
		
		then: "reponse status should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when update tutorial by id and with file then NOT FOUND"() {
		given: "create a tutorial in database"
			def id = createTutorialAsId()
			def tutorial = createTutorial(id);
	
		when: "update the tutorial throught API"
		  def status = sendRequestMultipart(Method.PUT.name(), "$API_URL", "/update/file", tutorial, (id+1), true)
		
		then: "reponse status should be OK"
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when publish all tutorials then OK"() {
		given: "create unpublished tutorial in database"
			createTutorialAsId()
		
		when: "publish all tutorials throug API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/publish")
  
		then: "result should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when publish tutorial by id then OK"() {
		given: "create unpublished tutorial in database"
			def id = createTutorialAsId()
		
		when: "publish tutorial by id through API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/publish", null, id, true)
  
		then: "result should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when publish tutorial by id then NOT FOUND"() {
		given: "create unpublished tutorial in database"
			def id = createTutorialAsId()
		
		when: "publish tutorial by id which is not in database through API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/publish", null, id+1, true)
  
		then: "result should be NOT_FOUND"
		  	status == HttpStatus.SC_NOT_FOUND
	}
	
	@Ignore
	def "when publish tutorials by ids then OK"() {
		given: "create unpublished tutorials in database"
			def id1 = createTutorialAsId()
			def id2 = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/publish/ids", null, buildMultiParam(["id1": id1, "id2": id2]), false)
			
		then: "result should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when publish tutorials by ids then NOT FOUND"() {
		given: "create unpublished tutorials in database"
			def id1 = createTutorialAsId()
			def id2 = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.PUT.name(), "$API_URL", "/publish/ids", null, buildMultiParam(["id1": id1, "id2": (id2+1)]), false)
		  
		then: "result should be OK"
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when delete all tutorials then OK"() {
		given: "create unpublished tutorials in database"
			def id1 = createTutorialAsId()
			def id2 = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/delete")
			
		then: "result should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete tutorial by id then OK"() {
		given: "create unpublished tutorials in database"
			def id = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/delete", null, id, true)
			
		then: "result should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete tutorial by id then NOT FOUND"() {
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/delete", null, 1, true)
			
		then: "result should be NOT FOUND"
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when delete tutorials by ids then OK"() {
		given: "create unpublished tutorials in database"
			def id1 = createTutorialAsId()
			def id2 = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/delete/ids", null, buildMultiParam(["id1": id1, "id2": id2]), false)
			
		then: "result should be NOT FOUND"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete tutorials by ids then NOT FOUND"() {
		given: "create unpublished tutorials in database"
			def id1 = createTutorialAsId()
			def id2 = createTutorialAsId()
			
		when: "publish tutorials by ids through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/delete/ids", null, buildMultiParam(["id1": id1, "id2": (id2+1)]), false)
			
		then: "result should be NOT FOUND"
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when create bucket then OK"() {
		given: "create new bucket instance"
		  def bucket = createBucket();
	
		when: "create bucket through API"
		  def status = sendRequest(Method.POST.name(), "$API_URL", "/bucket", bucket, true)
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_CREATED
	}

	@Ignore	
	def "when find all bucket then OK"() {
		given: "create bucket in database"
  		  def id = createBucketAsId();
	
		when: "find all buckets through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/bucket")
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when find bucket by id then OK"() {
		given: "create bucket in database"
  		  def id = createBucketAsId();
	
		when: "find all buckets through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/bucket/" + id)
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when find bucket by id not in DB then NOT FOUND"() {
		given: "create bucket in database"
  		  def id = createBucketAsId();
			println("CREATED ID = " + id);
	
		when: "find all buckets through API"
			println("FIND ID = " + id+1);
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/bucket/" + id+1)
  
		then: "response status should be NOT_FOUND"
			println("FIND STATUS = " + status);
		  	status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore	
	def "when find default bucket then OK"() {
		given: "create bucket in database"
  		  createBucketAsId(true);
	
		when: "find default bucket through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/bucket/default")
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete all buckets then OK"() {
		given: "create bucket in database"
  		  createBucketAsId();
	
		when: "delete all buckets through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/bucket")
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete bucket with id then OK"() {
		given: "create bucket in database"
  		  def id = createBucketAsId();
	
		when: "delete bucket with id through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/bucket/" + id)
  
		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}

	@Ignore
	def "when delete bucket with id not in DB then NOT_FOUND"() {
		given: "create bucket in database"
  		  def id = createBucketAsId();
	
		when: "delete bucket with id through API"
		  def status = sendRequest(Method.DELETE.name(), "$API_URL", "/bucket/" + id+1)
  
		then: "response status should be NOT FOUND"
		  	status == HttpStatus.SC_NOT_FOUND
	}
}
