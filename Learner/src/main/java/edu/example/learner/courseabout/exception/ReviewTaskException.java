package edu.example.learner.courseabout.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewTaskException extends RuntimeException {
    private String message;
    private int code;
}