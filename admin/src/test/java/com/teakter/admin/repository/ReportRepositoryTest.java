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
        .targetId("sdjfhie4hjkfghfjd")
        .reporterId("gjdhfiuernkgjdfgjk")
        .status(ReportStatus.PENDING)
        .build());
reportRepository.save(ReportEntity.builder()
        .targetId("hkfjgi8trhjkdf")
        .reporterId("hukghdkjndgkreng")
        .status(ReportStatus.DISMISSED).build());
reportRepository.save(ReportEntity.builder()
        .targetId("jgithgkjdfgf")
        .reporterId("gfjdhgi8 ynergjkd")
        .status(ReportStatus.PENDING)
        .build());

    List<ReportEntity> reports=reportRepository.findByStatus(ReportStatus.PENDING);

    assertEquals(2, reports.size());
    assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals("sdjfhie4hjkfghfjd")));
    assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals("jgithgkjdfgf")));
}

@Test
    void shouldFindByType(){
    reportRepository.save(ReportEntity.builder()
            .targetId("gjkdfhguehjfnbkgb")
            .reporterId("hjkdhg8idnhjdfbn")
            .type(ReportType.VIDEO)
            .build());
    reportRepository.save(ReportEntity.builder()
            .targetId("bjdfhgierngdfbjdmx")
            .reporterId("hkgjierntjgehsjtb")
            .type(ReportType.USER)
            .build());
    reportRepository.save(ReportEntity.builder()
            .targetId("hjkretnknv")
            .reporterId("hkretnerhvbsakdnitnbgvb")
            .type(ReportType.VIDEO)
            .build());

List<ReportEntity> reports=reportRepository.findByType(ReportType.VIDEO);

assertEquals(2, reports.size());
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals("gjkdfhguehjfnbkgb")));
assertTrue(reports.stream().anyMatch(r -> r.getTargetId().equals("hjkretnknv")));
}

}
