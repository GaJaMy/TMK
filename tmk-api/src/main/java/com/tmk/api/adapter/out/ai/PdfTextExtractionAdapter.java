package com.tmk.api.adapter.out.ai;

import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.TextExtractionPort;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
@Profile("!local & !test")
public class PdfTextExtractionAdapter implements TextExtractionPort {

    @Override
    public String extract(String source) {
        File file = new File(source);
        if (!file.exists()) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF: {}", text.length(), source);
            return text;
        } catch (IOException e) {
            log.error("Failed to extract text from PDF: {}", source, e);
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }
}
