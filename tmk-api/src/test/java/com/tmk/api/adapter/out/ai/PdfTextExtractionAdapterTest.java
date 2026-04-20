package com.tmk.api.adapter.out.ai;

import com.tmk.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfTextExtractionAdapterTest {

    private PdfTextExtractionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PdfTextExtractionAdapter();
    }

    @Test
    @DisplayName("extract throws BusinessException for non-existent file")
    void extract_throwsException_forNonExistentFile() {
        assertThatThrownBy(() -> adapter.extract("/nonexistent/file.pdf"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("extract throws BusinessException for invalid PDF file")
    void extract_throwsException_forInvalidPdf(@TempDir Path tempDir) throws IOException {
        File invalidFile = tempDir.resolve("invalid.pdf").toFile();
        invalidFile.createNewFile();

        assertThatThrownBy(() -> adapter.extract(invalidFile.getAbsolutePath()))
                .isInstanceOf(BusinessException.class);
    }
}
