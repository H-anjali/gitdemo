package com.anjali.manufacturing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;

import com.anjali.manufacturing.repository.MachineRepository;
import com.anjali.manufacturing.repository.ProcessLogRepository;
import com.anjali.manufacturing.model.Machine;
import com.anjali.manufacturing.model.ProcessLog;
import com.anjali.manufacturing.repository.UserRepository;
import com.anjali.manufacturing.model.User;
import com.anjali.manufacturing.service.OptimizationService; // Added for 112(b)

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class HomeController {

    private final MachineRepository machineRepository;
    private final ProcessLogRepository processLogRepository;
    private final UserRepository userRepository;
    private final OptimizationService optimizationService; // Added Service

    @Autowired
    public HomeController(MachineRepository machineRepository,
                          ProcessLogRepository processLogRepository,
                          UserRepository userRepository,
                          OptimizationService optimizationService) { // Injected Service
        this.machineRepository = machineRepository;
        this.processLogRepository = processLogRepository;
        this.userRepository = userRepository;
        this.optimizationService = optimizationService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/employee-login")
    public String employeeLogin() {
        return "employee-login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        List<Machine> machines = machineRepository.findAll();
        List<ProcessLog> logs = processLogRepository.findAll();

        // 1. MODULE: Bottleneck Detector (Statistical)
        // Instead of a simple loop, we use the Service's utilization math.
        String bottleneckResult = optimizationService.detectBottleneck();

        // We find the machine object by name so ${bottleneck.machineName} in HTML still works.
        Machine bottleneck = machines.stream()
                .filter(m -> bottleneckResult.contains(m.getMachineName()))
                .findFirst()
                .orElse(null);

        model.addAttribute("machines", machines);
        model.addAttribute("logs", logs);
        model.addAttribute("bottleneck", bottleneck); 

        return "admin-dashboard";
    }

    @PostMapping("/admin/add-machine")
    public String addMachine(@RequestParam String machineName,
                             @RequestParam int maxCapacity,
                             @RequestParam String status) {
        Machine m = new Machine();
        m.setMachineName(machineName);
        m.setMaxCapacity(maxCapacity);
        m.setStatus(status);
        machineRepository.save(m);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/delete-machine/{id}")
    public String deleteMachine(@PathVariable Long id) {
        machineRepository.deleteById(id);
        return "redirect:/admin/dashboard";
    }

   @GetMapping("/admin/report")
public String report(Model model) {
    List<ProcessLog> logs = processLogRepository.findAll();
    Map<String, Integer> reportData = new HashMap<>();

    for (ProcessLog log : logs) {
        String name = log.getMachine().getMachineName();
        reportData.put(name, reportData.getOrDefault(name, 0) + log.getUnitsProduced());
    }

    model.addAttribute("report", reportData); // Keeps your table working
    model.addAttribute("adminChartData", reportData); // For the new chart

    return "report";
}

    @GetMapping("/employee/dashboard")
    public String employeeDashboard(Model model) {
        model.addAttribute("machines", machineRepository.findAll());
        model.addAttribute("logs", processLogRepository.findAll());
        return "employee-dashboard";
    }

    @PostMapping("/employee/add-log")
    public String addLog(@RequestParam Long machineId,
                         @RequestParam String batchId,
                         @RequestParam int unitsProduced,
                         Model model) {

        Machine machine = machineRepository.findById(machineId).orElse(null);

        // 🚫 MODULE: Execution Controller (Transactional Job Dispatching)
        if(machine != null && "BREAKDOWN".equals(machine.getStatus())) {
            model.addAttribute("error", "Machine Under Breakdown. Cannot dispatch job.");

            // 🔎 MODULE: Optimization Unit (Schedule Rearrangement)
            // Suggest the best machine based on available capacity (Headroom).
            List<Machine> suggestedList = optimizationService.getOptimizedMachineSchedule();
            Machine suggested = suggestedList.isEmpty() ? null : suggestedList.get(0);

            model.addAttribute("suggestedMachine", suggested);
            model.addAttribute("machines", machineRepository.findAll());
            model.addAttribute("logs", processLogRepository.findAll());

            return "employee-dashboard";
        }

        ProcessLog log = new ProcessLog();
        log.setMachine(machine);
        log.setBatchId(batchId);
        log.setUnitsProduced(unitsProduced);
        processLogRepository.save(log);

        return "redirect:/employee/dashboard";
    }

    @GetMapping("/admin/edit-machine/{id}")
    public String editMachine(@PathVariable Long id, Model model) {
        Machine machine = machineRepository.findById(id).orElse(null);
        model.addAttribute("machine", machine);
        return "edit-machine";
    }

    @PostMapping("/admin/update-machine")
    public String updateMachine(@RequestParam Long machineId,
                                @RequestParam String machineName,
                                @RequestParam int maxCapacity,
                                @RequestParam String status) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine != null) {
            machine.setMachineName(machineName);
            machine.setMaxCapacity(maxCapacity);
            machine.setStatus(status);
            machineRepository.save(machine);
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/add-user")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/employee/update-status")
    public String updateStatus(@RequestParam Long machineId,
                               @RequestParam String status) {
        Machine machine = machineRepository.findById(machineId).orElse(null);
        if (machine != null) {
            machine.setStatus(status);
            machineRepository.save(machine);
        }
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/employee/performance")
public String performance(Model model) {
    List<ProcessLog> logs = processLogRepository.findAll();

    int totalUnits = logs.stream()
            .mapToInt(ProcessLog::getUnitsProduced)
            .sum();

    // NEW: Prepare data for the chart
    Map<String, Integer> chartData = new HashMap<>();
    for (ProcessLog log : logs) {
        String name = log.getMachine().getMachineName();
        chartData.put(name, chartData.getOrDefault(name, 0) + log.getUnitsProduced());
    }

    model.addAttribute("totalUnits", totalUnits);
    model.addAttribute("chartData", chartData); // Pass map to HTML

    return "performance";
}
}