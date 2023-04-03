package uk.gov.companieshouse.psc.delta.exception;

public class RetryableErrorException extends RuntimeException {
    public RetryableErrorException(String message) {
        super(message);
    }

    public RetryableErrorException(String message, Exception exception) {
        super(message, exception);
    }
}