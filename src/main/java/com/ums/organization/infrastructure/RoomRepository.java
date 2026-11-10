package com.ums.organization.infrastructure;

import com.ums.organization.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/** Database access for rooms (table rooms). Spring Data writes the SQL from the method names. */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * One room, skipping deleted ones.
     * <pre>
     * SELECT r.* FROM rooms r WHERE r.id = ? AND r.deleted_at IS NULL
     * </pre>
     *
     * @param id room id
     * @return the room, or empty when it does not exist or was deleted
     */
    Optional<Room> findByIdAndDeletedAtIsNull(Long id);

    /**
     * Whether the building already has a room with this code, deleted ones included: the database keeps
     * (building_id, code) UNIQUE even after a soft delete. The same code in another building is allowed.
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM rooms r WHERE r.building_id = ? AND r.code = ?)
     * </pre>
     *
     * @param buildingId building id
     * @param code       room code, e.g. R101
     * @return true when the code is taken in that building
     */
    boolean existsByBuildingIdAndCode(Long buildingId, String code);

    /**
     * Like {@link #existsByBuildingIdAndCode(Long, String)}, but ignores the room being updated
     * (it may keep its own code).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM rooms r WHERE r.building_id = ? AND r.code = ? AND r.id <> ?)
     * </pre>
     *
     * @param buildingId building id
     * @param code       room code, e.g. R101
     * @param id         id of the room being updated
     * @return true when another room in the building has the code
     */
    boolean existsByBuildingIdAndCodeAndIdNot(Long buildingId, String code, Long id);

    /**
     * Rooms for the list page, skipping deleted ones; q filters by code (empty q = all).
     * buildingId and roomType are optional filters: null = no filter.
     * Sorting and page size come from the Pageable (?page=1&size=20&sort=code,asc).
     *
     * @param q          search text; "" = all
     * @param buildingId only rooms of this building; null = every building
     * @param roomType   only rooms of this type (LECTURE, LAB …); null = every type
     * @param pageable   page number, size and sort
     * @return one page of rooms, with the totals
     */
    @Query("""
            select r from Room r
            where r.deletedAt is null
              and (:buildingId is null or r.building.id = :buildingId)
              and (:roomType is null or r.roomType = :roomType)
              and lower(r.code) like lower(concat('%', :q, '%'))
            """)
    Page<Room> search(String q, Long buildingId, Room.RoomType roomType, Pageable pageable);

    /**
     * Whether the building still has rooms (deleted ones not counted).
     * <pre>
     * SELECT EXISTS (SELECT 1 FROM rooms r WHERE r.building_id = ? AND r.deleted_at IS NULL)
     * </pre>
     *
     * @param buildingId building id
     * @return true when at least one room belongs to the building
     */
    boolean existsByBuildingIdAndDeletedAtIsNull(Long buildingId);

    /**
     * Whether the room is still used today, so it can't be deleted. Plain SQL on purpose: the tables belong to
     * other modules (academic, exam, asset), and organization must not import their repositories
     * (Spring Modulith rule, see ModularityTest). History (theses, events) and timetable drafts are not checked:
     * with soft delete the row stays, so they can still point to it.
     *
     * @param roomId room id
     * @return true when a weekly class, a planned exam (not deleted) or an asset (not deleted, not disposed)
     *         is in the room
     */
    @Query(value = """
            SELECT EXISTS (SELECT 1 FROM class_schedules cs WHERE cs.room_id = :roomId)
                OR EXISTS (SELECT 1 FROM exam_schedules es WHERE es.room_id = :roomId AND es.deleted_at IS NULL)
                OR EXISTS (SELECT 1 FROM assets a
                           WHERE a.room_id = :roomId AND a.deleted_at IS NULL AND a.status <> 'DISPOSED')
            """, nativeQuery = true)
    boolean isInUse(Long roomId);
}
