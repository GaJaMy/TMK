package com.tmk.core.port.out.persistence;

import com.tmk.core.question.entity.PrivateQuestion;
import java.util.List;
import java.util.Optional;

public interface PrivateQuestionPort {

    PrivateQuestion save(PrivateQuestion privateQuestion);

    List<PrivateQuestion> saveAll(List<PrivateQuestion> privateQuestions);

    Optional<PrivateQuestion> findById(Long privateQuestionId);

    Optional<PrivateQuestion> findByIdAndUserId(Long privateQuestionId, Long userId);

    List<PrivateQuestion> findAllByDocumentId(Long documentId);

    List<PrivateQuestion> findAllByDocumentIdLimit(Long documentId, int limit);

    long countByDocumentId(Long documentId);

    void deleteAllByDocumentId(Long documentId);
}
