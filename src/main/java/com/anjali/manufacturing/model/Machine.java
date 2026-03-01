package com.anjali.manufacturing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "machine")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long machineId;

    private String machineName;

    private int maxCapacity;

    private String status;   // must exist

    // GETTERS AND SETTERS

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}