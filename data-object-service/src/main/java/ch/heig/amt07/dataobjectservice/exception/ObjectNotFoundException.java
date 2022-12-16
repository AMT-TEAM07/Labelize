package ch.heig.amt07.dataobjectservice.exception;

public class ObjectNotFoundException extends AwsDataObjectException {
    public ObjectNotFoundException(String message) {
        super(message);
    }
}
