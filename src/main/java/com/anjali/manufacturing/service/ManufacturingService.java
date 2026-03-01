package com.anjali.manufacturing.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.anjali.manufacturing.model.Machine;
import com.anjali.manufacturing.model.ProcessLog;
import com.anjali.manufacturing.repository.MachineRepository;
import com.anjali.manufacturing.repository.ProcessLogRepository;

@Service
public class ManufacturingService {

    @Autowired
    private MachineRepository machineRepo;

    @Autowired
    private ProcessLogRepository processLogRepo;

    public List<Machine> getAllMachines() {
        return machineRepo.findAll();
    }

    public List<ProcessLog> getAllLogs() {
        return processLogRepo.findAll();
    }

    public void saveLog(ProcessLog log) {
        processLogRepo.save(log);
    }
}