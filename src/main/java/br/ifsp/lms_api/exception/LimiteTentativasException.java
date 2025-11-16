package br.ifsp.lms_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.FORBIDDEN) 
public class LimiteTentativasException extends RuntimeException {
    
    public LimiteTentativasException(String message) {
        super(message);
    }
}