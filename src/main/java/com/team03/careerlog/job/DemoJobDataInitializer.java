package com.team03.careerlog.job;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class DemoJobDataInitializer implements ApplicationRunner {

    private final JobPostingRepository repository;

    public DemoJobDataInitializer(JobPostingRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        LocalDate today = LocalDate.now();
        repository.saveAll(List.of(
                new JobPosting("demo-ux-1", "DEMO", "A회사", "UX 리서처 인턴",
                        "사용자 인터뷰 및 리서치 보조, 정성 데이터 정리와 인사이트 문서화, 프로덕트 팀 협업",
                        "https://example.com/jobs/ux-research", today.plusDays(14), "서울"),
                new JobPosting("demo-pm-1", "DEMO", "B회사", "서비스 기획 인턴",
                        "서비스 문제 정의와 개선안 도출, 시장 및 사용자 리서치, 기획 문서 작성과 협업",
                        "https://example.com/jobs/service-planning", today.plusDays(21), "서울"),
                new JobPosting("demo-backend-1", "DEMO", "C테크", "백엔드 개발 인턴",
                        "Java Spring Boot REST API 개발, PostgreSQL 데이터베이스 설계, 인증과 서버 배포 경험",
                        "https://example.com/jobs/backend", today.plusDays(30), "판교")
        ));
    }
}
