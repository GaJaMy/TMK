package com.tmk.api.question.dto;

public class OptionResult {

    private final int number;
    private final String content;

    public OptionResult(int number, String content) {
        this.number = number;
        this.content = content;
    }

    public int getNumber() { return number; }
    public String getContent() { return content; }
}
