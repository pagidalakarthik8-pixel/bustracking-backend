package com.bustrack.controller;

import com.bustrack.dto.Dtos.DriverRequest;
import com.bustrack.model.Driver;
import com.bustrack.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<Driver> list() {
        return driverService.findAll();
    }

    @GetMapping("/{id}")
    public Driver get(@PathVariable Long id) {
        return driverService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Driver create(@Valid @RequestBody DriverRequest req) {
        return driverService.create(req);
    }

    @PutMapping("/{id}")
    public Driver update(@PathVariable Long id, @Valid @RequestBody DriverRequest req) {
        return driverService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        driverService.delete(id);
    }
}
