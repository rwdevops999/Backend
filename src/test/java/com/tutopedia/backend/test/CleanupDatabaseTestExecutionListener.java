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
		System.out.println("===== DB CLEAR 1 =====");
	    if (!alreadyCleared) {
	        cleanupDatabase(testContext);
	        alreadyCleared = true;
	    }
	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		System.out.println("===== DB CLEAR 2 =====");
	    cleanupDatabase(testContext);
	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		System.out.println("===== DB CLEAR 3a =====");
	    if (testContext.getTestMethod().getAnnotation(ClearContext.class) != null) {
			System.out.println("===== DB CLEAR 3b =====");
	        cleanupDatabase(testContext);
	    }
	    
	    super.afterTestMethod(testContext);
	}
	private void cleanupDatabase(TestContext testContext) throws LiquibaseException {
		System.out.println("===== DB CLEAR 4 =====");
	    ApplicationContext app = testContext.getApplicationContext();
	    SpringLiquibase springLiquibase = app.getBean(SpringLiquibase.class);
	    springLiquibase.setDropFirst(true);
	    springLiquibase.afterPropertiesSet(); //The database get recreated here
	}
}