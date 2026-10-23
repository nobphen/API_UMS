package com.ums.common.storage.impl;

import com.ums.common.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "ums.storage.provider", havingValue = "s3")
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {
}
