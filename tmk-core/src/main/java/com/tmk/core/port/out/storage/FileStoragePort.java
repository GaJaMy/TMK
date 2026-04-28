package com.tmk.core.port.out.storage;

public interface FileStoragePort {

    String store(String originalFilename, byte[] bytes);

    void delete(String sourceReference);
}
