package com.tutopedia.backend.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({TutorialNotFoundException.class})
    protected ResponseEntity<Object> handleTutorialNotFound(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({TutorialIdMismatchException.class, FilePersistException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected ResponseEntity<Object> handleTutorialIdMismatch(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

//    @ExceptionHandler({FilePersistException.class})
//    protected ResponseEntity<Object> handleFilePersistError(Exception e, WebRequest request) {
//    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//    }

    @ExceptionHandler({BucketNotFoundException.class})
    protected ResponseEntity<Object> handleBucketNotFound(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({BucketDuplicateException.class})
    protected ResponseEntity<Object> handleBucketDuplicate(Exception e, WebRequest request) {
    	return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
