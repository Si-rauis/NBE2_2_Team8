package edu.example.learner.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.*;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class HeartNewsReqDTO {
    @NotNull(message = "Member must not be null")
    private Long memberId;
    @NotNull(message = "News must not be null")
    private Long newsId;

    public HeartNewsReqDTO(Long memberId, Long newsId) {
        this.memberId = memberId;
        this.newsId = newsId;
    }
}
