package com.tmk.api.exam.request;

public class CreateExamRequest {

    private String scope;
    private String topic;

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public CreateExamRequest() {}

    public CreateExamRequest(String scope, String topic) {
        this.scope = scope;
        this.topic = topic;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
