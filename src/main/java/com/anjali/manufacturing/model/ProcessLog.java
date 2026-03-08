package com.anjali.manufacturing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime; // Add this import

@Entity
public class ProcessLog {


// Add these fields to your ProcessLog class
private LocalDateTime startTime;
private LocalDateTime endTime;

// This allows us to calculate: 
// Actual Speed = unitsProduced / (endTime - startTime)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne
    @JoinColumn(name="machine_id")
    private Machine machine;

    private String batchId;
    private int unitsProduced;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public Machine getMachine() { return machine; }
    public void setMachine(Machine machine) { this.machine = machine; }

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }

    public int getUnitsProduced() { return unitsProduced; }
    public void setUnitsProduced(int unitsProduced) { this.unitsProduced = unitsProduced; }


    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}