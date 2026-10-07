package com.bustrack.service;

import com.bustrack.dto.Dtos.RouteRequest;
import com.bustrack.dto.Dtos.StopRequest;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Route;
import com.bustrack.model.Stop;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final BusRepository busRepository;

    public RouteService(RouteRepository routeRepository, BusRepository busRepository) {
        this.routeRepository = routeRepository;
        this.busRepository = busRepository;
    }

    @Transactional(readOnly = true)
    public List<Route> findAll() {
        return routeRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Route get(Long id) {
        return routeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    }

    @Transactional
    public Route create(RouteRequest req) {
        if (routeRepository.existsByName(req.name().trim())) {
            throw new BadRequestException("A route with this name already exists");
        }
        Route route = new Route();
        apply(route, req);
        return routeRepository.save(route);
    }

    @Transactional
    public Route update(Long id, RouteRequest req) {
        Route route = get(id);
        if (routeRepository.existsByNameAndIdNot(req.name().trim(), id)) {
            throw new BadRequestException("A route with this name already exists");
        }
        apply(route, req);
        return routeRepository.save(route);
    }

    @Transactional
    public void delete(Long id) {
        Route route = get(id);
        busRepository.clearRoute(id);
        routeRepository.delete(route);
    }

    private void apply(Route route, RouteRequest req) {
        route.setName(req.name().trim());
        route.setStartPoint(req.startPoint().trim());
        route.setEndPoint(req.endPoint().trim());
        route.setDistanceKm(req.distanceKm());
        route.getStops().clear();
        if (req.stops() != null) {
            req.stops().stream()
                    .sorted(Comparator.comparing(StopRequest::stopOrder))
                    .forEach(s -> {
                        Stop stop = new Stop();
                        stop.setName(s.name().trim());
                        stop.setStopOrder(s.stopOrder());
                        stop.setPickupTime(s.pickupTime());
                        stop.setRoute(route);
                        route.getStops().add(stop);
                    });
        }
    }
}
