package ru.cosmoscan.analysis.repository;

import ru.cosmoscan.analysis.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    Optional<Report> findByWorkId(Long workId);
}