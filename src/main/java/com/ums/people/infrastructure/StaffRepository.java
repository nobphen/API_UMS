package com.ums.people.infrastructure;

import com.ums.people.domain.Staff;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Database access for staff (table staff). */
public interface StaffRepository extends JpaRepository<Staff, Long> {

    /**
     * One staff member that is not deleted. Used by StaffService get, update, changeCode, changeStatus
     * and delete (empty = 404).
     * <pre>
     * SELECT s.* FROM staff s WHERE s.id = ? AND s.deleted_at IS NULL
     * </pre>
     *
     * @param id staff id
     * @return the staff member, or empty when it does not exist or is deleted
     */
    Optional<Staff> findByIdAndDeletedAtIsNull(Long id);

    /**
     * The staff record of a login, skipping deleted ones. Used by StaffService.getByUser and
     * createForExistingLogin (is the login already staff?), and by StudentService (reuse the staff's person).
     * <pre>
     * SELECT s.* FROM staff s WHERE s.user_id = ? AND s.deleted_at IS NULL
     * </pre>
     *
     * @param userId id of the login (users.id)
     * @return the staff record, or empty when the login is not staff
     */
    Optional<Staff> findByUserIdAndDeletedAtIsNull(Long userId);

    /**
     * Whether the person has a staff record (not deleted). Used by StudentService.delete.
     * <pre>
     * SELECT s.id FROM staff s WHERE s.person_id = ? AND s.deleted_at IS NULL FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param personId person id
     * @return true when the person is staff
     */
    boolean existsByPerson_IdAndDeletedAtIsNull(Long personId);

    /**
     * Whether a staff member already uses this staff code, ignoring case. Used by StaffService create and changeCode.
     * Deleted staff ARE counted on purpose: the database rule UNIQUE (staff_code) counts them too,
     * and a code is never used again.
     * <pre>
     * SELECT s.id FROM staff s WHERE upper(s.staff_code) = upper(?) FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param staffCode staff code to check
     * @return true when the code is taken
     */
    boolean existsByStaffCodeIgnoreCase(String staffCode);

    /**
     * Whether the department exists and is not deleted. Used by StaffService create and update.
     * Plain SQL on purpose: the department classes are in the organization module, which already uses people,
     * so importing them here would make a module cycle (same as BatchRepository.programExists).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM departments d WHERE d.id = ? AND d.deleted_at IS NULL)
     * </pre>
     *
     * @param departmentId department id
     * @return true when staff can be added to the department
     */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM departments d WHERE d.id = :departmentId AND d.deleted_at IS NULL)",
            nativeQuery = true)
    boolean departmentExists(@Param("departmentId") Long departmentId);

    /**
     * Whether the staff member is the dean of a faculty or the head of a department (not deleted ones).
     * Used by StaffService delete and changeStatus: choose a new dean or head first.
     * Plain SQL on purpose, for the same reason as {@link #departmentExists}.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM faculties f   WHERE f.dean_staff_id = ? AND f.deleted_at IS NULL)
     *     OR EXISTS (SELECT 1 FROM departments d WHERE d.head_staff_id = ? AND d.deleted_at IS NULL)
     * </pre>
     *
     * TODO: add these when their feature is built (the tables are empty until then):
     *   - class_section_lecturers: lecturer of a class with status OPEN (ClassSection feature)
     *   - hostels.warden_staff_id: warden of a hostel (Hostel feature)
     *   Then rename this method to isInUse.
     *
     * @param staffId staff id
     * @return true when the staff member is a dean or a department head
     */
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM faculties f WHERE f.dean_staff_id = :staffId AND f.deleted_at IS NULL)
                OR EXISTS (SELECT 1 FROM departments d WHERE d.head_staff_id = :staffId AND d.deleted_at IS NULL)
            """, nativeQuery = true)
    boolean isDeanOrHead(@Param("staffId") Long staffId);

    /**
     * Staff for the list page, skipping deleted ones. Each filter that is null is not used.
     * q filters by staff code or name, English or Khmer (empty q = all).
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=staffCode,asc).
     * The person is loaded in the same SELECT (join fetch), so reading the name needs no extra query.
     * <pre>
     * SELECT s.*, p.* FROM staff s JOIN persons p ON p.id = s.person_id
     * WHERE s.deleted_at IS NULL
     *   AND (? IS NULL OR s.department_id = ?) AND (? IS NULL OR s.staff_type = ?)
     *   AND (? IS NULL OR s.status = ?)
     *   AND (LOWER(s.staff_code) LIKE LOWER('%' || ? || '%') OR LOWER(p.first_name) LIKE ...
     *        OR LOWER(p.last_name) LIKE ... OR p.first_name_kh LIKE ... OR p.last_name_kh LIKE ...)
     * ORDER BY ... LIMIT ? OFFSET ?
     * </pre>
     * countQuery: the same WHERE with count(s), for the page totals (Spring can't build it by itself with join fetch).
     *
     * @param q            search text; "" = all
     * @param departmentId department id, or null for all departments
     * @param staffType    LECTURER, OFFICE, BOTH, or null for all
     * @param status       staff status, or null for all
     * @param pageable     page number, size and sort
     * @return one page of staff, with the totals
     */
    @Query(value = """
            select s from Staff s
            join fetch s.person p
            where s.deletedAt is null
              and (:departmentId is null or s.departmentId = :departmentId)
              and (:staffType is null or s.staffType = :staffType)
              and (:status is null or s.status = :status)
              and (lower(s.staffCode) like lower(concat('%', :q, '%'))
                   or lower(p.firstName) like lower(concat('%', :q, '%'))
                   or lower(p.lastName) like lower(concat('%', :q, '%'))
                   or p.firstNameKh like concat('%', :q, '%')
                   or p.lastNameKh like concat('%', :q, '%'))
            """,
           countQuery = """
            select count(s) from Staff s
            join s.person p
            where s.deletedAt is null
              and (:departmentId is null or s.departmentId = :departmentId)
              and (:staffType is null or s.staffType = :staffType)
              and (:status is null or s.status = :status)
              and (lower(s.staffCode) like lower(concat('%', :q, '%'))
                   or lower(p.firstName) like lower(concat('%', :q, '%'))
                   or lower(p.lastName) like lower(concat('%', :q, '%'))
                   or p.firstNameKh like concat('%', :q, '%')
                   or p.lastNameKh like concat('%', :q, '%'))
            """)
    Page<Staff> search(String q, Long departmentId, Staff.StaffType staffType,
                       Staff.Status status, Pageable pageable);
}
