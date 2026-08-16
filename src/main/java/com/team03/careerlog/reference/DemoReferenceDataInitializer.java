package com.team03.careerlog.reference;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DemoReferenceDataInitializer implements ApplicationRunner {

    private final ReferenceContentRepository repository;

    public DemoReferenceDataInitializer(ReferenceContentRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }

        repository.saveAll(List.of(
                new ReferenceContent(
                        "Spring Boot 공식 레퍼런스",
                        "https://docs.spring.io/spring-boot/reference/",
                        "ARTICLE", null, "Spring",
                        "Spring Boot 프로젝트 구조, 설정, 웹, 데이터베이스, 배포를 다루는 공식 문서"),
                new ReferenceContent(
                        "Spring Security 인증 가이드",
                        "https://docs.spring.io/spring-security/reference/features/authentication/index.html",
                        "ARTICLE", null, "Spring Security",
                        "로그인, 인증, 인가, 세션과 사용자 보안을 설명하는 공식 가이드"),
                new ReferenceContent(
                        "PostgreSQL 공식 튜토리얼",
                        "https://www.postgresql.org/docs/current/tutorial.html",
                        "ARTICLE", null, "PostgreSQL",
                        "PostgreSQL 데이터베이스, SQL, 테이블, 조인, 트랜잭션을 학습하는 공식 튜토리얼"),
                new ReferenceContent(
                        "GitHub Actions 빠른 시작",
                        "https://docs.github.com/en/actions/get-started/quickstart",
                        "ARTICLE", null, "GitHub",
                        "CI/CD 워크플로와 자동 테스트 및 배포를 시작하는 공식 가이드")
        ));
    }
}
