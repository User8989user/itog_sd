package ru.cosmoscan.storing.repository;

import ru.cosmoscan.storing.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {
}