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

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
public class HomeController {

    private final MachineRepository machineRepository;
    private final ProcessLogRepository processLogRepository;
    private final UserRepository userRepository;

    @Autowired
   public HomeController(MachineRepository machineRepository,
                      ProcessLogRepository processLogRepository,
                      UserRepository userRepository) {
    this.machineRepository = machineRepository;
    this.processLogRepository = processLogRepository;
    this.userRepository = userRepository;
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

    Map<Long, Integer> productionMap = new HashMap<>();

    for (ProcessLog log : logs) {
        Long machineId = log.getMachine().getMachineId();
        productionMap.put(machineId,
                productionMap.getOrDefault(machineId, 0)
                        + log.getUnitsProduced());
    }

    Machine bottleneck = null;
    int minProduction = Integer.MAX_VALUE;

    for (Machine m : machines) {
        int total = productionMap.getOrDefault(m.getMachineId(), 0);

        if (total < minProduction) {
            minProduction = total;
            bottleneck = m;
        }
    }

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

    Map<String, Integer> report = new HashMap<>();

    for (ProcessLog log : logs) {
        String name = log.getMachine().getMachineName();
        report.put(name,
                report.getOrDefault(name, 0)
                        + log.getUnitsProduced());
    }

    model.addAttribute("report", report);

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
                         @RequestParam int unitsProduced) {

        Machine machine = machineRepository.findById(machineId).orElse(null);

        if (machine != null) {
            ProcessLog log = new ProcessLog();
            log.setMachine(machine);
            log.setBatchId(batchId);
            log.setUnitsProduced(unitsProduced);

            processLogRepository.save(log);
        }

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

    machine.setMachineName(machineName);
    machine.setMaxCapacity(maxCapacity);
    machine.setStatus(status);

    machineRepository.save(machine);

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

    model.addAttribute("totalUnits", totalUnits);

    return "performance";
}
}