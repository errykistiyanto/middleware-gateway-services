package co.id.middleware.mock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class GeneralException extends RuntimeException {
    public GeneralException(String exception) {
        super(exception);
    }
}