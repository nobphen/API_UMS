package com.ums.asset.infrastructure;

import com.ums.asset.domain.AssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {
}
