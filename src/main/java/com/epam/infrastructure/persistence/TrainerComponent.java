package com.epam.infrastructure.persistence;

import com.epam.infrastructure.daos.TrainerSummaryDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerComponent extends JpaRepository<TrainerSummaryDao, UUID> {
    Optional<TrainerSummaryDao> findByUsername(String username);
}
