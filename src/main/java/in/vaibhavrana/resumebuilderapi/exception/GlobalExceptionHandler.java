package in.vaibhavrana.resumebuilderapi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidationException(MethodArgumentNotValidException ex){
        log.info("Inside GlobalExceptionHandler-handleValidationException()");
        Map<String,String> errors=new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error->{
            String fieldName=((FieldError)error).getField();
            String errorMessage=error.getDefaultMessage();
            errors.put(fieldName,errorMessage);
        });
        Map<String,Object> response=new HashMap<>();
        response.put("message","Validation failed");
        response.put("errors",errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }
    @ExceptionHandler(ResourceExistsException.class)
    public ResponseEntity<Map<String,Object>> handleResourceExistsException(ResourceExistsException ex){
        log.info("Inside GlobalExceptionHandler-handleResourceExistsException()");
        Map<String,Object> response=new HashMap<>();
        response.put("message","Resource exists");
        response.put("errors", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    public ResponseEntity<Map<String,Object>> handlerGenericException(Exception ex){
        log.info("Inside GlobalExceptionHandler-handlerGenericException()");
        Map<String,Object> response=new HashMap<>();
        response.put("message","Something went wrong. Contact Administrator");
        response.put("errors", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
