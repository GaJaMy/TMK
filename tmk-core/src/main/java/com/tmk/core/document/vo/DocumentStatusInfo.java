package com.tmk.core.document.vo;

import com.tmk.core.document.entity.Document;

public record DocumentStatusInfo(
    Document document,
    long chunkCount,
    long questionCount
) {}
