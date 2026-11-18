package com.ums.common.storage;

/**
 * Saves, deletes and gives a link to uploaded files (photos now; documents, assignments later), in an S3 server:
 * RustFS in docker-compose for dev, Amazon S3 / Cloudflare R2 / RustFS in production (only the config changes).
 * The database keeps only the KEY (e.g. persons/7/0c9f….jpg), never a link: links expire, keys don't.
 */
public interface FileStorageService {

    /**
     * Saves the bytes under a new random name, so two uploads never overwrite each other and nobody can guess
     * the name. When a transaction is running and is rolled back, the new file is deleted again.
     *
     * @param content   the file, already checked and cleaned (e.g. {@link ImageFiles#clean})
     * @param folder    where it goes, e.g. "persons/7"
     * @param extension the real type, e.g. "jpg" (from {@link ImageFiles#require}, never from the client)
     * @return the key to save in the database, e.g. "persons/7/0c9f2b1e-….jpg"
     */
    String store(byte[] content, String folder, String extension);

    /**
     * Deletes the file. Inside a transaction it waits until the commit: if the request fails, the old file
     * is still there and the database still points to it. A missing file or a null key does nothing.
     *
     * @param key the key from {@link #store}; null = nothing to delete
     */
    void delete(String key);

    /**
     * A signed link the app can open without a token. It stops working after ums.storage.s3.link-ttl (15 min),
     * so a leaked link is useless later; the app simply asks the API again for a new one.
     *
     * @param key the key from {@link #store}
     * @return e.g. "http://localhost:9000/ums/persons/7/0c9f….jpg?X-Amz-Expires=900&X-Amz-Signature=…";
     *         null when the key is null
     */
    String url(String key);
}
