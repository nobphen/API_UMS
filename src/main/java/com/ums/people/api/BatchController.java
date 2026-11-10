package com.ums.people.api;

import com.ums.common.response.ApiResponse;
import com.ums.common.security.RequirePermission;
import com.ums.people.PeoplePermissions;
import com.ums.people.api.dto.BatchRequest;
import com.ums.people.api.dto.BatchResponse;
import com.ums.people.application.BatchService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Batches: the groups of students of one program who started in the same year (e.g. "Batch 12", 2026).
 * Reading is open to every logged-in user, like the other master data (Program, Room): the student form's
 * batch dropdown, the student app ("my batch"), timetables. A batch is only a name and a year, no personal data.
 * Creating, changing and deleting need BATCH_MANAGE (REGISTRAR).
 */
@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    /**
     * Adds a batch to a program. 201. The name must be free in the program (ignoring case), deleted batches included.
     * <pre>POST /api/v1/batches  {"programId": 1, "name": "Batch 12", "nameKh": "ជំនាន់ទី១២", "startYear": 2026}</pre>
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequirePermission(PeoplePermissions.BATCH_MANAGE)
    public ApiResponse<BatchResponse> create(@Valid @RequestBody BatchRequest request) {
        return ApiResponse.created(batchService.create(request));
    }

    /**
     * One batch; 404 when it does not exist or was deleted.
     * <pre>GET /api/v1/batches/1</pre>
     */
    @GetMapping("/{id}")
    public ApiResponse<BatchResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(batchService.get(id));
    }

    /**
     * All batches of one program, newest start year first, deleted ones left out. No paging: a program has only
     * about one batch a year, and the dropdown needs all of them. programId is required (400 when missing).
     * <pre>GET /api/v1/batches?programId=1</pre>
     */
    @GetMapping
    public ApiResponse<List<BatchResponse>> list(@RequestParam Long programId) {
        return ApiResponse.ok(batchService.list(programId));
    }

    /**
     * Replaces all the batch's fields (send every field, like for create). A batch with students can't move
     * to another program (400).
     * <pre>PUT /api/v1/batches/1  {"programId": 1, "name": "Batch 12", "nameKh": "ជំនាន់ទី១២", "startYear": 2026}</pre>
     */
    @PutMapping("/{id}")
    @RequirePermission(PeoplePermissions.BATCH_MANAGE)
    public ApiResponse<BatchResponse> update(@PathVariable Long id, @Valid @RequestBody BatchRequest request) {
        return ApiResponse.updated(batchService.update(id, request));
    }

    /**
     * Soft delete: the row stays for history, but the batch is no longer listed.
     * Refused (400) while students are still in the batch.
     * <pre>DELETE /api/v1/batches/1</pre>
     */
    @DeleteMapping("/{id}")
    @RequirePermission(PeoplePermissions.BATCH_MANAGE)
    public ApiResponse<Void> delete(@PathVariable Long id) {
        batchService.delete(id);
        return ApiResponse.deleted();
    }
}
