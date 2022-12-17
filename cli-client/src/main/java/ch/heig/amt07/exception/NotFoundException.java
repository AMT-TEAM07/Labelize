package ch.heig.amt07.exception;

public class NotFoundException extends HttpException {
    public NotFoundException(String message) {
        super(message);
    }
}
