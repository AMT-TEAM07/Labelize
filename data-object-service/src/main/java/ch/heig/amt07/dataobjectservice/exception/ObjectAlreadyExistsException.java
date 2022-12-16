package ch.heig.amt07.dataobjectservice.exception;

public class ObjectAlreadyExistsException extends AwsDataObjectException {
    public ObjectAlreadyExistsException(String message) {
        super(message);
    }
}
