package com.ums.common.storage.impl;

import com.ums.common.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** Not written yet. Later: keeps files in Amazon S3; used when ums.storage.provider is s3. */
@Service
@ConditionalOnProperty(name = "ums.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {
}
