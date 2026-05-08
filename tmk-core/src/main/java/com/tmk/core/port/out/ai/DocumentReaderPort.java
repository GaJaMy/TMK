package com.tmk.core.port.out.ai;

public interface DocumentReaderPort {

    String readPdf(String sourceReference);

    String readMarkdown(String sourceReference);
}
