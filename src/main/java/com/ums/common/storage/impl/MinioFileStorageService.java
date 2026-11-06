package com.ums.common.storage.impl;

import com.ums.common.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Not written yet. Later: keeps files in MinIO; used when ums.storage.provider is minio or not set. */
@Service
@ConditionalOnProperty(name = "ums.storage.provider", havingValue = "minio", matchIfMissing = true)
@RequiredArgsConstructor
public class MinioFileStorageService implements FileStorageService {
}
