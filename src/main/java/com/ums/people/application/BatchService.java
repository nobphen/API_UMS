package com.ums.people.application;

import com.ums.people.api.dto.BatchRequest;
import com.ums.people.api.dto.BatchResponse;
import java.util.List;

/**
 * Batches: the groups of students who started one program in the same year (e.g. "Batch 12", 2026).
 * Create, read, change and (soft) delete. Used by BatchController.
 */
public interface BatchService {

    /**
     * Adds a batch to a program. The names are trimmed (" Batch 12 " → "Batch 12").
     *
     * @param request program, name, Khmer name and start year
     * @return the new batch, with its id
     * @throws com.ums.common.exception.NotFoundException error.program.not_found when the program does not exist
     *                                                    or was deleted
     * @throws com.ums.common.exception.BusinessException error.batch.name_exists when the program already has a batch
     *                                                    with the name (ignoring case), a deleted one included
     */
    BatchResponse create(BatchRequest request);

    /**
     * One batch.
     *
     * @param id batch id
     * @return the batch
     * @throws com.ums.common.exception.NotFoundException error.batch.not_found when it does not exist or was deleted
     */
    BatchResponse get(Long id);

    /**
     * All batches of one program, deleted ones left out, newest start year first. No paging: a program has
     * only about one batch a year. Used by the batch dropdown of the student form.
     *
     * @param programId program id
     * @return the program's batches; empty when it has none
     */
    List<BatchResponse> list(Long programId);

    /**
     * Replaces all the batch's fields with the request (names trimmed).
     *
     * @param id      batch id
     * @param request the new values; the batch may keep its own name
     * @return the batch after the change
     * @throws com.ums.common.exception.NotFoundException error.batch.not_found or error.program.not_found when it
     *                                                    does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.batch.name_exists when another batch of the program
     *                                                    has the name; error.batch.program_locked when the program
     *                                                    changes but the batch already has students
     */
    BatchResponse update(Long id, BatchRequest request);

    /**
     * Soft delete: sets deleted_at, the row stays for history. Its name can't be used again in the program.
     *
     * @param id batch id
     * @throws com.ums.common.exception.NotFoundException error.batch.not_found when it does not exist or was deleted
     * @throws com.ums.common.exception.BusinessException error.batch.in_use while students are still in the batch
     */
    void delete(Long id);
}
