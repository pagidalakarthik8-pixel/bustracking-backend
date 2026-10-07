package com.bustrack.controller;

import com.bustrack.dto.Dtos.BusRequest;
import com.bustrack.dto.Dtos.BusStatusRequest;
import com.bustrack.dto.Dtos.BusLocationRequest;
import com.bustrack.model.Bus;
import com.bustrack.service.BusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public List<Bus> list() {
        return busService.findAll();
    }

    @GetMapping("/{id}")
    public Bus get(@PathVariable Long id) {
        return busService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Bus create(@Valid @RequestBody BusRequest req) {
        return busService.create(req);
    }

    @PutMapping("/{id}")
    public Bus update(@PathVariable Long id, @Valid @RequestBody BusRequest req) {
        return busService.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public Bus updateStatus(@PathVariable Long id, @Valid @RequestBody BusStatusRequest req) {
        return busService.updateStatus(id, req);
    }

    /** GPS-device endpoint. Restricted to administrators until dedicated device credentials are configured. */
    @PatchMapping("/{id}/location")
    public Bus updateLocation(@PathVariable Long id, @Valid @RequestBody BusLocationRequest req) {
        return busService.updateLocation(id, req.latitude(), req.longitude());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        busService.delete(id);
    }
}
