package com.tmk.api.user.topic.controller;

import com.tmk.api.common.ApiResponse;
import com.tmk.api.user.topic.dto.TopicResponse;
import com.tmk.api.user.topic.usecase.TopicUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TopicController implements TopicControllerDocs {

    private final TopicUseCase topicUseCase;

    @GetMapping("/topics")
    @Override
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getTopics() {
        return ApiResponse.ok(topicUseCase.getTopics());
    }
}
