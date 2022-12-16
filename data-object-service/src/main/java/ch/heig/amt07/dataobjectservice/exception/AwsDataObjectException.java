package ch.heig.amt07.dataobjectservice.exception;

public class AwsDataObjectException extends RuntimeException {
    public AwsDataObjectException(String message) {
        super(message);
    }
}