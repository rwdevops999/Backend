@Library("shared-library@main") _

def isValid = true;

pipeline {
	agent {
		label 'macos'
	}

	environment {
    	PATH = "/Applications/Docker.app/Contents/Resources/bin:${env.PATH}"
    	USER = 'rwdevops999'
    	IMAGE = 'topbackend'
  	}
  
	stages {
		stage("info") {
			steps {
			    sh 'mvn -v'
			    sh 'docker -v'
			}

		}
		
		stage("clean") {
			steps {
				sh 'mvn clean'	    
			}
		}

		stage("build") {
			steps {
				sh 'mvn install -DskipTests'	    
			}
			
			post {
			    failure {
    			    script {
    			        isValid = false
    			    }
    			}
			}

		}
		
		stage("test") {
			when {
			    expression {
    			   isValid
    			}
			}

			steps {
				sh 'export spring_profiles_active=test'	    
				sh 'mvn test'	    
			}
			
			post {
			    success {
			        junit '**/test-results/**/*.xml'
			    }

				failure {
				    script {
    				    isValid = false
    				}
				}
			}

		}

		stage("package") {
			when {
			    expression {
    			   isValid
    			}
			}

			environment {
			    DOCKERHUB_ACCESSKEY = credentials('DockerHubUserPassword')
			    KEYCHAIN_PSW = credentials('keychain')
			}
			
			steps {
				sh '''
					security unlock-keychain -p ${KEYCHAIN_PSW}
					docker login -u ${DOCKERHUB_ACCESSKEY_USR} -p ${DOCKERHUB_ACCESSKEY_PSW}
					docker build . -t ${IMAGE}
				'''
			}
			
			post {
			    failure {
			        script {
        			    isValid = false
        			}
			    }

			}

		}

		stage("publish") {
			when {
			    expression {
    			   isValid
    			}
			}

			steps {
				sh '''
					docker logout registry-1.docker.io
					docker tag ${IMAGE} ${USER}/${IMAGE}
					docker push ${USER}/${IMAGE}
				'''
			}
		}
	}
	
	post {
	  success {
	    mailTo(to: 'rudi.welter@gmail.com', attachLog: true)
	  }

	  failure {
	    mailTo(to: 'rudi.welter@gmail.com', attachLog: true)
	  }
	}	
}