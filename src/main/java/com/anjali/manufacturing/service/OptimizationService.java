package com.anjali.manufacturing.service;

import com.anjali.manufacturing.model.Machine;
import com.anjali.manufacturing.model.ProcessLog;
import com.anjali.manufacturing.repository.MachineRepository;
import com.anjali.manufacturing.repository.ProcessLogRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OptimizationService {

    private final MachineRepository machineRepository;
    private final ProcessLogRepository logRepository;

    public OptimizationService(MachineRepository machineRepository, ProcessLogRepository logRepository) {
        this.machineRepository = machineRepository;
        this.logRepository = logRepository;
    }

    /**
     * MODULE: Optimization Unit
     * Strategy: Load Balancing & Throughput Maximization.
     * Logic: Suggests ACTIVE machines with the most "Available Headroom" 
     * (Capacity - Current Units Produced).
     */
    public List<Machine> getOptimizedMachineSchedule() {
        List<Machine> machines = machineRepository.findAll();
        List<ProcessLog> logs = logRepository.findAll();

        // Map to store current load per machine
        Map<Long, Integer> currentLoadMap = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getMachine().getMachineId(),
                Collectors.summingInt(ProcessLog::getUnitsProduced)
            ));

        return machines.stream()
            .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus())) // Constraint: Only operational machines
            .sorted((m1, m2) -> {
                int load1 = currentLoadMap.getOrDefault(m1.getMachineId(), 0);
                int load2 = currentLoadMap.getOrDefault(m2.getMachineId(), 0);
                
                // Heuristic: Calculate available capacity (Headroom)
                int headroom1 = m1.getMaxCapacity() - load1;
                int headroom2 = m2.getMaxCapacity() - load2;
                
                // Prioritize machines with more headroom to maximize throughput
                return Integer.compare(headroom2, headroom1);
            })
            .collect(Collectors.toList());
    }

    /**
     * MODULE: Bottleneck Detector
     * Logic: Statistical Throughput Analysis using Utilization (rho = lambda / mu).
     * Identifies the machine closest to 100% capacity.
     */
    public String detectBottleneck() {
        List<Machine> machines = machineRepository.findAll();
        List<ProcessLog> logs = logRepository.findAll();
        
        Machine bottleneckMachine = null;
        double highestUtilization = 0.0;

        for (Machine m : machines) {
            int totalProduced = logs.stream()
                .filter(log -> log.getMachine().getMachineId().equals(m.getMachineId()))
                .mapToInt(ProcessLog::getUnitsProduced)
                .sum();

            // Utilization Rate: (Units Produced / Machine Rated Capacity)
            double utilization = (double) totalProduced / m.getMaxCapacity();

            if (utilization > highestUtilization) {
                highestUtilization = utilization;
                bottleneckMachine = m;
            }
        }

        if (bottleneckMachine == null || highestUtilization == 0) {
            return "Stable (No Bottlenecks)";
        }

        return bottleneckMachine.getMachineName() + " (" + String.format("%.1f", highestUtilization * 100) + "% Load)";
    }
}