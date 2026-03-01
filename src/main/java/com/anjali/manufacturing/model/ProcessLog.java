package com.anjali.manufacturing.model;

import jakarta.persistence.*;

@Entity
public class ProcessLog {

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
}