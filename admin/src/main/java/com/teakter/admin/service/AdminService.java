package com.teakter.admin.service;

import com.teakter.admin.dto.ReportDetailDto;
import com.teakter.admin.entity.PredefinedReason;
import com.teakter.admin.entity.ReportType;

import java.util.List;

public interface AdminService {

void moderateVideo(String videoId, String reason);
void moderateUser(String userId, String reason);
void createReport(ReportType type, String targetId, PredefinedReason reason, String description);
List<ReportDetailDto> getPendingReports();
void dismissReport(String targetId, ReportType type);

}
