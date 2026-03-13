package com.tmk.api.exam.usecase;

import com.tmk.api.exam.dto.*;
import com.tmk.api.question.dto.OptionResult;
import com.tmk.core.exam.entity.Exam;
import com.tmk.core.exam.entity.ExamQuestion;
import com.tmk.core.exam.service.*;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExamUseCase {

    private final CreateExamService createExamService;
    private final GetExamService getExamService;
    private final SaveAnswerService saveAnswerService;
    private final SubmitExamService submitExamService;
    private final GetExamResultService getExamResultService;
    private final GetExamHistoryService getExamHistoryService;
    private final GetExamHistoryDetailService getExamHistoryDetailService;
    private final QuestionPort questionPort;

    public ExamResult create(Long userId) {
        Exam exam = createExamService.create(userId);
        return new ExamResult(
                exam.getId(),
                exam.getTotalQuestions(),
                exam.getTimeLimit(),
                exam.getStartedAt().toString(),
                exam.getExpiredAt().toString(),
                exam.getStatus().name()
        );
    }

    public ExamDetailResult getExam(Long examId, Long userId) {
        Exam exam = getExamService.getExam(examId, userId);

        // Load question details for each exam question
        List<Long> questionIds = exam.getExamQuestions().stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionIds.stream()
                .map(id -> questionPort.findById(id).orElseThrow())
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<ExamQuestionResult> questions = exam.getExamQuestions().stream()
                .map(eq -> {
                    Question q = questionMap.get(eq.getQuestionId());
                    List<OptionResult> options = q.getOptions().stream()
                            .map(opt -> new OptionResult(opt.getOptionNumber(), opt.getContent()))
                            .collect(Collectors.toList());
                    return new ExamQuestionResult(
                            q.getId(),
                            eq.getOrderNum(),
                            q.getContent(),
                            q.getType().name(),
                            q.getDifficulty().name(),
                            options,
                            eq.getMyAnswer()
                    );
                })
                .collect(Collectors.toList());

        return new ExamDetailResult(
                exam.getId(),
                exam.getStatus().name(),
                exam.getExpiredAt().toString(),
                questions
        );
    }

    public void saveAnswers(Long examId, Long userId, List<AnswerCommand> answers) {
        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(
                        AnswerCommand::getQuestionId,
                        AnswerCommand::getAnswer
                ));
        saveAnswerService.saveAnswers(examId, userId, answerMap);
    }

    public SubmitResult submit(Long examId, Long userId) {
        Exam exam = submitExamService.submit(examId, userId);
        int correctCount = (int) exam.getExamQuestions().stream()
                .filter(eq -> Boolean.TRUE.equals(eq.getIsCorrect()))
                .count();
        int total = exam.getTotalQuestions();
        double score = total > 0 ? (double) correctCount / total * 100 : 0;
        boolean passed = score >= 50.0;

        return new SubmitResult(
                exam.getId(),
                total,
                correctCount,
                score,
                passed,
                exam.getSubmittedAt() != null ? exam.getSubmittedAt().toString() : null
        );
    }

    public ExamResultData getResult(Long examId, Long userId) {
        Exam exam = getExamResultService.getResult(examId, userId);
        int correctCount = (int) exam.getExamQuestions().stream()
                .filter(eq -> Boolean.TRUE.equals(eq.getIsCorrect()))
                .count();
        int total = exam.getTotalQuestions();
        double score = total > 0 ? (double) correctCount / total * 100 : 0;
        boolean passed = score >= 50.0;

        return new ExamResultData(
                exam.getId(),
                total,
                correctCount,
                score,
                passed,
                exam.getSubmittedAt() != null ? exam.getSubmittedAt().toString() : null
        );
    }

    public HistoryListResult getHistory(Long userId, int page, int size) {
        List<Exam> exams = getExamHistoryService.getHistory(userId, page, size);
        long total = getExamHistoryService.count(userId);
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        List<HistorySummary> summaries = exams.stream()
                .map(exam -> {
                    int correctCount = (int) exam.getExamQuestions().stream()
                            .filter(eq -> Boolean.TRUE.equals(eq.getIsCorrect()))
                            .count();
                    int totalQ = exam.getTotalQuestions();
                    double score = totalQ > 0 ? (double) correctCount / totalQ * 100 : 0;
                    return new HistorySummary(
                            exam.getId(),
                            totalQ,
                            correctCount,
                            score,
                            score >= 50.0,
                            exam.getSubmittedAt() != null ? exam.getSubmittedAt().toString() : null
                    );
                })
                .collect(Collectors.toList());

        return new HistoryListResult(summaries, page, size, total, totalPages);
    }

    public HistoryDetailResult getHistoryDetail(Long examId, Long userId) {
        Exam exam = getExamHistoryDetailService.getHistoryDetail(examId, userId);

        // Load question details
        List<Long> questionIds = exam.getExamQuestions().stream()
                .map(ExamQuestion::getQuestionId)
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionIds.stream()
                .map(id -> questionPort.findById(id).orElseThrow())
                .collect(Collectors.toMap(Question::getId, q -> q));

        int correctCount = (int) exam.getExamQuestions().stream()
                .filter(eq -> Boolean.TRUE.equals(eq.getIsCorrect()))
                .count();
        int total = exam.getTotalQuestions();
        double score = total > 0 ? (double) correctCount / total * 100 : 0;

        List<HistoryQuestionResult> questions = exam.getExamQuestions().stream()
                .map(eq -> {
                    Question q = questionMap.get(eq.getQuestionId());
                    List<OptionResult> options = q.getOptions().stream()
                            .map(opt -> new OptionResult(opt.getOptionNumber(), opt.getContent()))
                            .collect(Collectors.toList());
                    return new HistoryQuestionResult(
                            q.getId(),
                            eq.getOrderNum(),
                            q.getContent(),
                            q.getType().name(),
                            q.getDifficulty().name(),
                            options,
                            eq.getMyAnswer(),
                            q.getAnswer(),
                            q.getExplanation(),
                            Boolean.TRUE.equals(eq.getIsCorrect())
                    );
                })
                .collect(Collectors.toList());

        return new HistoryDetailResult(
                exam.getId(),
                total,
                correctCount,
                score,
                score >= 50.0,
                exam.getSubmittedAt() != null ? exam.getSubmittedAt().toString() : null,
                questions
        );
    }
}
