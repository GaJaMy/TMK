package com.tmk.api.adapter.out.ai;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.DocumentReaderPort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApiDocumentReaderAdapter implements DocumentReaderPort {

    @Override
    public String readPdf(String sourceReference) {
        Path target = Path.of(sourceReference).toAbsolutePath().normalize();
        try (PDDocument document = Loader.loadPDF(Files.readAllBytes(target))) {
            return new PDFTextStripper().getText(document);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    @Override
    public String readMarkdown(String sourceReference) {
        Path target = Path.of(sourceReference).toAbsolutePath().normalize();
        try {
            return Files.readString(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }
}
