package com.tmk.api.user.document.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class DocumentUploadRequest {

    @NotBlank(message = "문서 제목은 필수입니다.")
    private String title;

    @NotNull(message = "업로드 파일은 필수입니다.")
    private MultipartFile file;
}
