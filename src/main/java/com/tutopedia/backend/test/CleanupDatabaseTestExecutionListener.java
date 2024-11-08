package com.tutopedia.backend.test;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

public class CleanupDatabaseTestExecutionListener extends AbstractTestExecutionListener {

	private boolean alreadyCleared = false;

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
	    if (!alreadyCleared) {
	        cleanupDatabase(testContext);
	        alreadyCleared = true;
	    } else {
	        alreadyCleared = true;
	    }
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
	    cleanupDatabase(testContext);
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
	    if (testContext.getTestMethod().getAnnotation(ClearContext.class) != null) {
	        cleanupDatabase(testContext);
	    }
	    
	    super.afterTestMethod(testContext);
	}
	private void cleanupDatabase(TestContext testContext) throws LiquibaseException {
	    ApplicationContext app = testContext.getApplicationContext();
	    SpringLiquibase springLiquibase = app.getBean(SpringLiquibase.class);
	    springLiquibase.setDropFirst(true);
	    springLiquibase.afterPropertiesSet(); //The database get recreated here
	}
}