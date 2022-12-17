package ch.heig.amt07.exception;

public class InternalServerErrorException extends HttpException {
    public InternalServerErrorException(String message) {
        super(message);
    }
}
