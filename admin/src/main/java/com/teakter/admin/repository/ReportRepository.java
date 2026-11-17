package com.teakter.admin.repository;

import com.teakter.admin.entity.ReportEntity;
import com.teakter.admin.entity.ReportStatus;
import com.teakter.admin.entity.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {
List<ReportEntity> findByStatus(ReportStatus status);
List<ReportEntity> findByType(ReportType type);
List<ReportEntity> findByTargetIdAndTypeAndStatus(String targetId, ReportType type, ReportStatus status);

}
