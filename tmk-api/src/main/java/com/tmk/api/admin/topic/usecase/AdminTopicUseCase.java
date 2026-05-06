package com.tmk.api.admin.topic.usecase;

import com.tmk.api.admin.topic.dto.AdminTopicResponse;
import com.tmk.api.admin.topic.request.AdminTopicCreateRequest;
import com.tmk.api.admin.topic.result.AdminTopicResult;
import com.tmk.api.admin.topic.service.AdminTopicService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminTopicUseCase {

    private final AdminTopicService adminTopicService;

    @Transactional(readOnly = true)
    public List<AdminTopicResponse> getTopics() {
        List<AdminTopicResult> results = adminTopicService.getTopics();
        return results.stream()
                .map(AdminTopicResponse::from)
                .toList();
    }

    @Transactional
    public AdminTopicResponse createTopic(Long createdByAdminId, AdminTopicCreateRequest request) {
        AdminTopicResult result = adminTopicService.createTopic(createdByAdminId, request.name());
        return AdminTopicResponse.from(result);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        adminTopicService.deleteTopic(topicId);
    }
}
