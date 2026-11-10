package com.ums.people.application.impl;

import com.ums.common.exception.BusinessException;
import com.ums.common.exception.NotFoundException;
import com.ums.people.api.dto.BatchRequest;
import com.ums.people.api.dto.BatchResponse;
import com.ums.people.application.BatchService;
import com.ums.people.application.mapper.BatchMapper;
import com.ums.people.domain.Batch;
import com.ums.people.infrastructure.BatchRepository;
import com.ums.people.infrastructure.StudentRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * See {@link BatchService}. A batch is one row in batches; its program is checked with plain SQL
 * (BatchRepository.programExists), because people can't import the organization module.
 * Every method runs in one transaction (@Transactional on the class): when a rule fails, nothing is saved.
 * Reads use readOnly = true, which is a little faster.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final StudentRepository studentRepository;   // delete and program change: students in the batch
    private final BatchMapper batchMapper;               // request → entity → response (MapStruct)

    /** New batch: the program must exist and the name must be free in it, then INSERT. */
    @Override
    public BatchResponse create(BatchRequest request) {

        // 1. The program must exist and not be deleted
        requireProgram(request.programId());

        // 2. The name must be free in this program (the same name in another program is OK)
        String name = request.name().trim();
        if (batchRepository.existsByProgramIdAndNameIgnoreCase(request.programId(), name)) {
            throw new BusinessException("error.batch.name_exists", name);
        }

        // 3. Copy the request into a new entity, then set the trimmed names
        Batch batch = batchMapper.toEntity(request);
        batch.setName(name);
        batch.setNameKh(blankToNull(request.nameKh()));

        // 4. INSERT, then answer with the saved batch (it has its id now)
        return batchMapper.toResponse(batchRepository.save(batch));
    }

    /** One batch by id; 404 when it does not exist or was deleted. */
    @Override
    @Transactional(readOnly = true)
    public BatchResponse get(Long id) {
        return batchMapper.toResponse(findBatch(id));
    }

    /** The program's batches (not deleted), newest start year first, each row → response. */
    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> list(Long programId) {
        return batchRepository.findByProgramIdAndDeletedAtIsNullOrderByStartYearDescNameAsc(programId)
                .stream()
                .map(batchMapper::toResponse)
                .toList();
    }

    /** Check the batch, its program and its new name, copy the request onto it; JPA saves at commit. */
    @Override
    public BatchResponse update(Long id, BatchRequest request) {

        // 1. The batch must exist and not be deleted
        Batch batch = findBatch(id);

        // 2. The program must exist and not be deleted
        requireProgram(request.programId());

        // 3. A batch with students can't move to another program: the students would be in a batch
        //    that is not of their program
        boolean programChanged = !batch.getProgramId().equals(request.programId());
        if (programChanged && studentRepository.existsByBatchIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.batch.program_locked");
        }

        // 4. The new name must be free in the program; its own name is allowed (IdNot skips this batch)
        String name = request.name().trim();
        if (batchRepository.existsByProgramIdAndNameIgnoreCaseAndIdNot(request.programId(), name, id)) {
            throw new BusinessException("error.batch.name_exists", name);
        }

        // 5. Copy the request onto the batch, then set the trimmed names
        batchMapper.update(request, batch);
        batch.setName(name);
        batch.setNameKh(blankToNull(request.nameKh()));

        // 6. No save() needed: the batch was loaded in this transaction, so JPA writes the UPDATE at commit
        return batchMapper.toResponse(batch);
    }

    /** Soft delete: refused while students are in the batch, otherwise set deletedAt. */
    @Override
    public void delete(Long id) {

        // 1. The batch must exist and not be deleted already
        Batch batch = findBatch(id);

        // 2. Students are still in the batch: refuse (move them to another batch first)
        if (studentRepository.existsByBatchIdAndDeletedAtIsNull(id)) {
            throw new BusinessException("error.batch.in_use");
        }

        // 3. Soft delete: the row stays, GET no longer shows it (UPDATE at commit, like in update)
        batch.setDeletedAt(OffsetDateTime.now());
    }

    /** The batch, or 404 when it does not exist or was deleted. Used by get, update and delete. */
    private Batch findBatch(Long id) {
        return batchRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("error.batch.not_found"));
    }

    /** 404 when the program does not exist or was deleted. Used by create and update. */
    private void requireProgram(Long programId) {
        if (!batchRepository.programExists(programId)) {
            throw new NotFoundException("error.program.not_found");
        }
    }

    /** " ជំនាន់ទី១២ " → "ជំនាន់ទី១២"; null or only spaces → null, so an empty Khmer name is saved as NULL. */
    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
