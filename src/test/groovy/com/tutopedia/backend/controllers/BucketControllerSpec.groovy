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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tutopedia.backend.BackendApplication
import com.tutopedia.backend.persistence.model.Bucket
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

@SpringBootTest(
	  webEnvironment = WebEnvironment.DEFINED_PORT,
	  classes = BackendApplication.class)
@TestPropertySource(
	  locations = "classpath:application-test.properties")
@TutorialTest 
class BucketControllerSpec extends Specification {
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
			Bucket bucket = (Bucket)body
			def multiPartHttpEntity = builder
				.addTextBody("name", bucket.name)
				.addTextBody("selected", bucket.isSelected().toString())
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
		def API_PATH = properties."API_PATH_BUCKET"

		API_URL = "$API_URI:$API_PORT$API_PATH"
		println "API = " + API_URL
		
		httpclient = HttpClients.createDefault()
		entitybuilder = MultipartEntityBuilder.create();
		entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
	}

	/**
	 * create a bucket instance with 
	 * - random name
	 * - selected flag passed as parameter
	 * - favorite flag defautt false
	 * - tutorials value default 0
	 *
	 * @param id: optional id value
	 * @param name: optional name value	
	 * @param name: selected boolean value	
	 * @return bucket
	 */
	private def createBucket(
		def id = null, 
		def name = null, 
		def selected = null ) {
		Bucket bucket = new Bucket();
		
		if (id != null) {
			bucket.id = id
		}
		
		bucket.name = name
		if (name == null) {
			bucket.name= RandomStringUtils.randomAlphabetic(10)
		}
		
		if (selected == null) {
			bucket.selected = false
		} else {
			bucket.selected = selected
		}

		bucket.favorite = false;
		bucket.tutorials = 0;
		bucket.updateDate = new Date();
		
		return bucket
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
	 * create a bucket in database through API
	 * 	
	 * @return id of bucket or 0L if not created
	 */
	private def createBucketAsId() {
		Bucket bucket = createBucket();
		
		entitybuilder.addTextBody("name", bucket.name)
		entitybuilder.addTextBody("selected", bucket.selected.toString())
		entitybuilder.addTextBody("favorite", "false")
		entitybuilder.addTextBody("tutorials", String.valueOf(bucket.tutorials))
		
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
	
	private def createBucketAsId(Bucket bucket) {
		entitybuilder.addTextBody("name", bucket.name)
		entitybuilder.addTextBody("selected", bucket.selected.toString())
		entitybuilder.addTextBody("favorite", "false")
		entitybuilder.addTextBody("tutorials", String.valueOf(bucket.tutorials))
		
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
	def "when find all buckets then OK"() {
		when: "find all buckets through API"
		  def status = sendRequest(Method.GET.name(), "$API_URL", "/find")

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

	@Ignore
	def "create 1 bucket, when find all buckets then should retrieve that one"() {
		given: "create a bucket"
		createBucketAsId();
		
		when: "find all bucket through API"
			RestTemplate restTemplate = new RestTemplate()
			List<Bucket> buckets = restTemplate.getForObject("$API_URL/find", List.class)
			
		then: "list should contain 1 bucket"
		buckets.size() == 1;
	}	

	@Ignore
	def "delete all buckets, when find all buckets then should retrieve none"() {
		given: "delete all buckets"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
		
		when: "find all bucket through API"
			List<Bucket> buckets = restTemplate.getForObject("$API_URL/find", List.class)
			
		then: "list should contain 1 bucket"
		buckets.size() == 0;
	}	

	@Ignore
	def "when find default bucket then OK"() {
		given: "create default bucket in db API"
		  def bucket = createBucket(123, "defaultbucket", true)
		  def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", bucket)
		  
		when: "find default bucket through API"
		  status = sendRequest(Method.GET.name(), "$API_URL", "/default")

		then: "response status should be OK"
		  	status == HttpStatus.SC_OK
	}	

	@Ignore
	def "when find default bucket then should be selected"() {
		given: "create default bucket in db API"
		  def bucket = createBucket(123, "defaultbucket", true)
		  def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", bucket)
		  
		when: "find default bucket through API"
			RestTemplate restTemplate = new RestTemplate()
			Bucket defaultBucket = restTemplate.getForObject("$API_URL/default", Bucket.class)

		then: "bucket should be selected"
		  	defaultBucket.isSelected() == true
	}	

	@Ignore
	def "when find default bucket then should none present when not created"() {
		given: "delete all buckets and create non-default bucket in db API"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
			def bucket = createBucket(123, "bucket")
			def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", bucket)
		  
		when: "find default bucket through API"
			Bucket defaultBucket = restTemplate.getForObject("$API_URL/default", Bucket.class)

		then: "no bucket found"
			  defaultBucket == null
	}

	@Ignore
	def "when create bucket then CREATED"() {
		given: "setup multipart data"
			def bucket = createBucket();
	
		when: "create bucket in database using API"
			def status = sendRequestMultipart(Method.POST.name(), "$API_URL", "/create", bucket)
			
		then: "response status should be CREATED"
			status == HttpStatus.SC_CREATED
	}

	@Ignore
	def "when delete bucket by id then bucket removed"() {
		given: "setup multipart data"
			def id = createBucketAsId()
			
		when: "delete bucket in database using API"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete/"+id)

		then: "response status should be NOT FOUND"
			def status = sendRequest(Method.GET.name(), "$API_URL", "/find/"+id)
			status == HttpStatus.SC_NOT_FOUND
	}

	@Ignore
	def "when update default bucket then SC_OK"() {
		given: "create bucket"
			def id = createBucketAsId()
			
		when: "update default bucket in database using API"
			def status = sendRequest(Method.PUT.name(), "$API_URL", "/default/"+id)

		then: "response status should be OK"
			status == HttpStatus.SC_OK
	}

	@Ignore
	def "when update default bucket then SC_NOT_FOUND"() {
		given: "delete all buckets"
			RestTemplate restTemplate = new RestTemplate()
			restTemplate.delete("$API_URL/delete")
			
		when: "update default bucket in database using API"
			def status = sendRequest(Method.PUT.name(), "$API_URL", "/default/"+123)

		then: "response status should be NOT FOUND"
			status == HttpStatus.SC_NOT_FOUND
	}

	def "when update default bucket then old default bucket is not default, new bucket is default"() {
		given: "create default and non default bucket"
			def defaultBucket = createBucket(null, "default", true)
			Long defaultId = createBucketAsId(defaultBucket)
			println ("DEFAULT ID = " + defaultId)
			def nonDefaultBucket = createBucket(null, "nondefault")
			Long nonDefaultId = createBucketAsId(nonDefaultBucket)
			println ("NON DEFAULT ID = " + nonDefaultId)
			
		when: "update non default bucket to default"
			sendRequest(Method.PUT.name(), "$API_URL", "/default/" + nonDefaultId)
			
		then: "old default bucket should be non-default"
			RestTemplate restTemplate = new RestTemplate()
			def bucket = restTemplate.getForObject("$API_URL/find/"+defaultId, Bucket.class);
			def newbucket = restTemplate.getForObject("$API_URL/find/"+nonDefaultId, Bucket.class);
			bucket.isSelected() == false
			newbucket.isSelected() == true;

			
	}
}
