package com.bustrack.controller;

import com.bustrack.dto.Dtos.ScheduleRequest;
import com.bustrack.model.Schedule;
import com.bustrack.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public List<Schedule> list(@RequestParam(required = false) Long busId) {
        return scheduleService.find(busId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Schedule create(@Valid @RequestBody ScheduleRequest req) {
        return scheduleService.create(req);
    }

    @PutMapping("/{id}")
    public Schedule update(@PathVariable Long id, @Valid @RequestBody ScheduleRequest req) {
        return scheduleService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        scheduleService.delete(id);
    }
}
