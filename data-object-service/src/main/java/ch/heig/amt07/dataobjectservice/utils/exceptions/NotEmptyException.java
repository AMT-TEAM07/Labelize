package ch.heig.amt07.dataobjectservice.utils.exceptions;

public class NotEmptyException extends RuntimeException {
    public NotEmptyException(String message) {
        super(message);
    }
}
