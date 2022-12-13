package ch.heig.amt07.dataobjectservice.exception;

public class NotEmptyException extends AwsDataObjectException {
    public NotEmptyException(String message) {
        super(message);
    }
}
