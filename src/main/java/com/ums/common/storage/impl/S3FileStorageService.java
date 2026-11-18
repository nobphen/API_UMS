package com.ums.common.storage.impl;

import com.ums.common.storage.FileStorageService;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Keeps files in an S3 server (ums.storage.s3.*): RustFS from docker-compose.yml in dev; Amazon S3,
 * Cloudflare R2 or RustFS on a server in production. Only the config differs, never this code.
 * <pre>
 * bucket "ums"
 *   └ persons/7/0c9f2b1e-….jpg      ← key (saved in persons.photo_url)
 * </pre>
 * The two S3 clients are made on the first use, so the app (and the tests) start even without an S3 server.
 */
@Slf4j
@Service
public class S3FileStorageService implements FileStorageService {

    /** The Content-Type saved with the file, so a browser shows it as an image. */
    private static final Map<String, String> CONTENT_TYPES = Map.of("jpg", "image/jpeg", "png", "image/png");

    private final String endpoint;
    private final String publicEndpoint;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final Duration linkTtl;

    private volatile S3Client client;          // uploads and deletes (endpoint)
    private volatile S3Presigner presigner;    // signed links (public-endpoint: the address the app can reach)
    private volatile boolean bucketReady;      // the bucket was checked / created once

    /** Written by hand (no @RequiredArgsConstructor) because every value comes from application.yml. */
    public S3FileStorageService(@Value("${ums.storage.s3.endpoint}") String endpoint,
                                @Value("${ums.storage.s3.public-endpoint}") String publicEndpoint,
                                @Value("${ums.storage.s3.region}") String region,
                                @Value("${ums.storage.s3.access-key}") String accessKey,
                                @Value("${ums.storage.s3.secret-key}") String secretKey,
                                @Value("${ums.storage.s3.bucket}") String bucket,
                                @Value("${ums.storage.s3.link-ttl}") Duration linkTtl) {
        this.endpoint = endpoint;
        this.publicEndpoint = publicEndpoint;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.linkTtl = linkTtl;
    }

    /** New random name → upload → delete it again if the transaction is rolled back. */
    @Override
    public String store(byte[] content, String folder, String extension) {

        // 1. A new name nobody can guess: persons/7/0c9f2b1e-….jpg
        String key = folder + "/" + UUID.randomUUID() + "." + extension;

        // 2. Upload (the bucket is created on the very first upload)
        ensureBucket();
        client().putObject(put -> put.bucket(bucket).key(key)
                        .contentType(CONTENT_TYPES.getOrDefault(extension, "application/octet-stream")),
                RequestBody.fromBytes(content));

        // 3. The request failed after this (rollback)? Then the database never points to the file: remove it
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        deleteNow(key);
                    }
                }
            });
        }
        return key;
    }

    /** Inside a transaction: delete after the commit. Outside: delete now. */
    @Override
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteNow(key);
                }
            });
        } else {
            deleteNow(key);
        }
    }

    /** A GET link signed with the secret key, valid for link-ttl. Made locally: no call to the S3 server. */
    @Override
    public String url(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return presigner().presignGetObject(link -> link
                        .signatureDuration(linkTtl)
                        .getObjectRequest(get -> get.bucket(bucket).key(key)))
                .url().toString();
    }

    /// HELPERS ------------------------------------------------------

    /** Deletes the file; a problem is only logged (the request already succeeded). */
    private void deleteNow(String key) {
        try {
            client().deleteObject(delete -> delete.bucket(bucket).key(key));
        } catch (RuntimeException e) {
            log.warn("Could not delete the file {}: {}", key, e.getMessage());
        }
    }

    /** Creates the bucket when it does not exist yet (a new RustFS container is empty). */
    private void ensureBucket() {
        if (bucketReady) {
            return;
        }
        try {
            client().headBucket(head -> head.bucket(bucket));
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
            client().createBucket(create -> create.bucket(bucket));
            log.info("Created the S3 bucket {}", bucket);
        }
        bucketReady = true;
    }

    /** The upload client, made on the first use. */
    private S3Client client() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = S3Client.builder()
                            .endpointOverride(URI.create(endpoint))
                            .region(Region.of(region))
                            .credentialsProvider(credentials())
                            .forcePathStyle(true)   // http://host:9000/ums/key, not http://ums.host:9000/key (RustFS)
                            .build();
                }
            }
        }
        return client;
    }

    /** The link signer, made on the first use. */
    private S3Presigner presigner() {
        if (presigner == null) {
            synchronized (this) {
                if (presigner == null) {
                    presigner = S3Presigner.builder()
                            .endpointOverride(URI.create(publicEndpoint))
                            .region(Region.of(region))
                            .credentialsProvider(credentials())
                            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                            .build();
                }
            }
        }
        return presigner;
    }

    /** The access and secret key, or a clear error when they are not set. */
    private StaticCredentialsProvider credentials() {
        if (accessKey == null || accessKey.isBlank() || secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Set ums.storage.s3.access-key and secret-key (S3_ACCESS_KEY / S3_SECRET_KEY)");
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }
}
