package co.id.middleware.finnet.exception;

import java.time.LocalDateTime;

public class ExceptionResponse {
    private LocalDateTime timestamp;
    private String responseCode;
    private String responseMessage;
    private String details;

    public ExceptionResponse(LocalDateTime timestamp, String responseCode, String responseMessage, String details) {
        super();
        this.timestamp = timestamp;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getDetails() {
        return details;
    }

}
