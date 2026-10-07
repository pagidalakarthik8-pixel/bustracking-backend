package com.bustrack.controller;

import com.bustrack.dto.Dtos.RouteRequest;
import com.bustrack.model.Route;
import com.bustrack.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<Route> list() {
        return routeService.findAll();
    }

    @GetMapping("/{id}")
    public Route get(@PathVariable Long id) {
        return routeService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Route create(@Valid @RequestBody RouteRequest req) {
        return routeService.create(req);
    }

    @PutMapping("/{id}")
    public Route update(@PathVariable Long id, @Valid @RequestBody RouteRequest req) {
        return routeService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        routeService.delete(id);
    }
}
