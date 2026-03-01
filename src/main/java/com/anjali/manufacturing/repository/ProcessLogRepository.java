package com.anjali.manufacturing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.anjali.manufacturing.model.ProcessLog;

@Repository
public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {
}