package com.anjali.manufacturing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.anjali.manufacturing.model.Machine;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
}