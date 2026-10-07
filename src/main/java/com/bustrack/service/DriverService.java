package com.bustrack.service;

import com.bustrack.dto.Dtos.DriverRequest;
import com.bustrack.exception.BadRequestException;
import com.bustrack.exception.ResourceNotFoundException;
import com.bustrack.model.Driver;
import com.bustrack.repository.BusRepository;
import com.bustrack.repository.DriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final BusRepository busRepository;

    public DriverService(DriverRepository driverRepository, BusRepository busRepository) {
        this.driverRepository = driverRepository;
        this.busRepository = busRepository;
    }

    @Transactional(readOnly = true)
    public List<Driver> findAll() {
        return driverRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Driver get(Long id) {
        return driverRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Driver not found"));
    }

    @Transactional
    public Driver create(DriverRequest req) {
        if (driverRepository.existsByLicenseNumber(req.licenseNumber().trim())) {
            throw new BadRequestException("A driver with this license number already exists");
        }
        Driver d = new Driver();
        apply(d, req);
        return driverRepository.save(d);
    }

    @Transactional
    public Driver update(Long id, DriverRequest req) {
        Driver d = get(id);
        if (driverRepository.existsByLicenseNumberAndIdNot(req.licenseNumber().trim(), id)) {
            throw new BadRequestException("A driver with this license number already exists");
        }
        apply(d, req);
        return driverRepository.save(d);
    }

    @Transactional
    public void delete(Long id) {
        Driver d = get(id);
        busRepository.clearDriver(id);
        driverRepository.delete(d);
    }

    private void apply(Driver d, DriverRequest req) {
        d.setName(req.name().trim());
        d.setPhone(req.phone().trim());
        d.setLicenseNumber(req.licenseNumber().trim());
    }
}
