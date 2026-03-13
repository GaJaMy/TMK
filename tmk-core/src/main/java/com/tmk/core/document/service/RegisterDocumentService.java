package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.port.out.DocumentPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterDocumentService {

    private final DocumentPort documentPort;

    public Document register(String title, String source) {
        // TODO
        return null;
    }
}
