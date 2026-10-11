package com.teakter.admin.repository;

import com.teakter.admin.entity.ReportEntity;
import com.teakter.admin.entity.ReportStatus;
import com.teakter.admin.entity.ReportType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReportRepositoryTest {

    @Autowired
private ReportRepository reportRepository;

@Test
    void shouldFindByStatus(){
reportRepository.save(ReportEntity.builder()
        .targetId(1L)
        .reporterId(10L)
        .status(ReportStatus.PENDING)
        .build());
reportRepository.save(ReportEntity.builder()
        .targetId(2L)
        .reporterId(20L)
        .status(ReportStatus.DISMISSED).build());
reportRepository.save(ReportEntity.builder()
        .targetId(3L)
        .reporterId(30L)
        .status(ReportStatus.PENDING)
        .build());

    List<ReportEntity> reports=reportRepository.findByStatus(ReportStatus.PENDING);

    assertEquals(2, reports.size());
    assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(1L)));
    assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(3L)));
}

@Test
    void shouldFindByType(){
    reportRepository.save(ReportEntity.builder()
            .targetId(1L)
            .reporterId(10L)
            .type(ReportType.VIDEO)
            .build());
    reportRepository.save(ReportEntity.builder()
            .targetId(2L)
            .reporterId(20L)
            .type(ReportType.USER)
            .build());
    reportRepository.save(ReportEntity.builder()
            .targetId(3L)
            .reporterId(30L)
            .type(ReportType.VIDEO)
            .build());

List<ReportEntity> reports=reportRepository.findByType(ReportType.VIDEO);

assertEquals(2, reports.size());
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(1L)));
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals(3L)));
}

}
