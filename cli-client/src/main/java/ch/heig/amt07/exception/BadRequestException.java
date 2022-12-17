package ch.heig.amt07.exception;

public class BadRequestException extends HttpException {
    public BadRequestException(String message) {
        super(message);
    }
}
