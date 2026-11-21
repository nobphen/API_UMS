package com.ums.asset.infrastructure;

import com.ums.asset.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for assets (table assets: computers, projectors, furniture ... and the room they are in). */
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Whether an asset (not deleted) is still in this room, except assets with the given status
     * (e.g. DISPOSED: thrown away, so it no longer counts). Not used yet (for the room "in use" check later).
     * <pre>
     * SELECT a.id FROM assets a
     * WHERE a.room_id = ? AND a.deleted_at IS NULL AND a.status <> ? FETCH FIRST 1 ROWS ONLY
     * </pre>
     *
     * @param roomId room id
     * @param status the status that does not count, e.g. DISPOSED
     * @return true when at least one asset is in the room
     */
    boolean existsByRoomIdAndDeletedAtIsNullAndStatusNot(Long roomId, Asset.Status status);
}
