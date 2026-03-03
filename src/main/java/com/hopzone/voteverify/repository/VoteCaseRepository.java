package com.hopzone.voteverify.repository;

import com.hopzone.voteverify.entity.VoteCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface VoteCaseRepository extends JpaRepository<VoteCase, Long>, JpaSpecificationExecutor<VoteCase> {

    Optional<VoteCase> findByReportHash(String reportHash);

    List<VoteCase> findByCreatedAtUtcBefore(LocalDateTime cutoff);
}
