package com.tmk.api.question.support;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;
import org.springframework.util.StringUtils;

public final class QuestionAnswerSupport {

    private QuestionAnswerSupport() {
    }

    public static String normalizeStoredAnswer(QuestionType type, String answer, List<QuestionOptionCandidate> options) {
        if (!StringUtils.hasText(answer)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String normalizedAnswer = answer.trim();
        if (type == QuestionType.SHORT_ANSWER) {
            return normalizedAnswer;
        }

        if (isNumeric(normalizedAnswer)) {
            short optionNumber = Short.parseShort(normalizedAnswer);
            validateOptionNumber(optionNumber, options);
            return String.valueOf(optionNumber);
        }

        short optionNumber = resolveOptionNumber(type, normalizedAnswer, options);
        return String.valueOf(optionNumber);
    }

    public static String toDisplayAnswer(QuestionType type, String storedAnswer, List<QuestionOptionCandidate> options) {
        if (!StringUtils.hasText(storedAnswer)) {
            return storedAnswer;
        }
        if (type == QuestionType.SHORT_ANSWER) {
            return storedAnswer;
        }

        String normalizedAnswer = storedAnswer.trim();
        if (!isNumeric(normalizedAnswer)) {
            return normalizedAnswer;
        }

        short optionNumber = Short.parseShort(normalizedAnswer);
        return options.stream()
                .filter(option -> option.optionNumber() == optionNumber)
                .map(QuestionOptionCandidate::content)
                .findFirst()
                .orElse(normalizedAnswer);
    }

    public static boolean isCorrectAnswer(QuestionType type, String submittedAnswer, String storedAnswer) {
        if (!StringUtils.hasText(submittedAnswer) || !StringUtils.hasText(storedAnswer)) {
            return false;
        }
        if (type == QuestionType.SHORT_ANSWER) {
            return submittedAnswer.trim().equalsIgnoreCase(storedAnswer.trim());
        }
        return submittedAnswer.trim().equals(storedAnswer.trim());
    }

    private static short resolveOptionNumber(
            QuestionType type,
            String normalizedAnswer,
            List<QuestionOptionCandidate> options
    ) {
        if (type == QuestionType.TRUE_FALSE) {
            String normalizedBooleanAnswer = normalizeTrueFalseAnswer(normalizedAnswer);
            if (normalizedBooleanAnswer != null) {
                return options.stream()
                        .filter(option -> option.content().equalsIgnoreCase(normalizedBooleanAnswer))
                        .map(QuestionOptionCandidate::optionNumber)
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
            }
        }

        return options.stream()
                .filter(option -> option.content().equalsIgnoreCase(normalizedAnswer))
                .map(QuestionOptionCandidate::optionNumber)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT));
    }

    private static String normalizeTrueFalseAnswer(String answer) {
        String normalizedAnswer = answer.trim();
        if ("참".equalsIgnoreCase(normalizedAnswer) || "true".equalsIgnoreCase(normalizedAnswer) || "o".equalsIgnoreCase(normalizedAnswer)) {
            return "참";
        }
        if ("거짓".equalsIgnoreCase(normalizedAnswer) || "false".equalsIgnoreCase(normalizedAnswer) || "x".equalsIgnoreCase(normalizedAnswer)) {
            return "거짓";
        }
        return null;
    }

    private static void validateOptionNumber(short optionNumber, List<QuestionOptionCandidate> options) {
        boolean exists = options.stream()
                .anyMatch(option -> option.optionNumber() == optionNumber);
        if (!exists) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private static boolean isNumeric(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isDigit(value.charAt(index))) {
                return false;
            }
        }
        return true;
    }

    public record QuestionOptionCandidate(short optionNumber, String content) {
    }
}
