package edu.example.learner.exception;

public class MemberTaskException extends RuntimeException {

    private final int statusCode;

    public MemberTaskException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
