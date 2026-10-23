package com.ums.asset.infrastructure;

import com.ums.asset.domain.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {
}
