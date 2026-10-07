package com.bustrack.config;

import com.bustrack.model.*;
import com.bustrack.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

/** Creates the default admin account and a little demo data the first time the app starts. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final ScheduleRepository scheduleRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bustrack.seed.enabled}")
    private boolean enabled;
    @Value("${bustrack.seed.admin-email}")
    private String adminEmail;
    @Value("${bustrack.seed.admin-password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, DriverRepository driverRepository,
                      RouteRepository routeRepository, BusRepository busRepository,
                      ScheduleRepository scheduleRepository, NotificationRepository notificationRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.routeRepository = routeRepository;
        this.busRepository = busRepository;
        this.scheduleRepository = scheduleRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled || userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setName("Transport Admin");
        admin.setEmail(adminEmail.toLowerCase());
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        Driver d1 = driver("Ramesh Kumar", "9876500001", "TS09-2015-001122");
        Driver d2 = driver("Suresh Reddy", "9876500002", "TS09-2012-003344");
        Driver d3 = driver("Venkat Rao", "9876500003", "TS09-2018-005566");

        Route r1 = route("Route 1 - Uppal to Campus", "Uppal", "Anurag Campus", 24.5,
                new String[]{"Uppal Ring Road", "Habsiguda", "Tarnaka", "Ghatkesar Cross"},
                new int[][]{{7, 0}, {7, 15}, {7, 30}, {7, 55}});
        Route r2 = route("Route 2 - Kukatpally to Campus", "Kukatpally", "Anurag Campus", 38.0,
                new String[]{"KPHB Colony", "Moosapet", "Ameerpet", "Secunderabad"},
                new int[][]{{6, 30}, {6, 45}, {7, 10}, {7, 35}});
        Route r3 = route("Route 3 - LB Nagar to Campus", "LB Nagar", "Anurag Campus", 31.2,
                new String[]{"Dilsukhnagar", "Nagole", "Uppal Depot"},
                new int[][]{{6, 45}, {7, 0}, {7, 25}});

        Bus b1 = bus("BT-01", "TS08 UA 1021", 50, d1, r1, BusStatus.ON_TIME, null);
        Bus b2 = bus("BT-02", "TS08 UB 2043", 55, d2, r2, BusStatus.DELAYED, "Running about 15 minutes late due to traffic");
        Bus b3 = bus("BT-03", "TS08 UC 3087", 45, d3, r3, BusStatus.ON_TIME, null);

        for (Bus b : new Bus[]{b1, b2, b3}) {
            schedule(b, LocalTime.of(6, 30), LocalTime.of(8, 45), "MORNING");
            schedule(b, LocalTime.of(16, 30), LocalTime.of(18, 45), "EVENING");
        }

        User student = new User();
        student.setName("Demo Student");
        student.setEmail("student@bustrack.com");
        student.setPassword(passwordEncoder.encode("student123"));
        student.setRole(Role.STUDENT);
        student.setRollNumber("24EG110000");
        student.setPhone("9000000000");
        student.setBus(b1);
        student.setBoardingStop("Habsiguda");
        userRepository.save(student);

        notification("Welcome to Anurag University Bus Tracking",
                "Check your bus timings and live status here. Contact the transport office for route changes.", null);
        notification("Bus BT-02 status update",
                "Bus BT-02 is now delayed: Running about 15 minutes late due to traffic", b2);

        log.info("Seeded demo data. Admin login: {} / {}  |  Student login: student@bustrack.com / student123",
                adminEmail, adminPassword);
    }

    private Driver driver(String name, String phone, String license) {
        Driver d = new Driver();
        d.setName(name);
        d.setPhone(phone);
        d.setLicenseNumber(license);
        return driverRepository.save(d);
    }

    private Route route(String name, String start, String end, double km, String[] stopNames, int[][] times) {
        Route r = new Route();
        r.setName(name);
        r.setStartPoint(start);
        r.setEndPoint(end);
        r.setDistanceKm(km);
        for (int i = 0; i < stopNames.length; i++) {
            Stop s = new Stop();
            s.setName(stopNames[i]);
            s.setStopOrder(i + 1);
            s.setPickupTime(LocalTime.of(times[i][0], times[i][1]));
            s.setRoute(r);
            r.getStops().add(s);
        }
        return routeRepository.save(r);
    }

    private Bus bus(String number, String reg, int capacity, Driver d, Route r, BusStatus status, String note) {
        Bus b = new Bus();
        b.setBusNumber(number);
        b.setRegistrationNumber(reg);
        b.setCapacity(capacity);
        b.setDriver(d);
        b.setRoute(r);
        b.setStatus(status);
        b.setStatusNote(note);
        return busRepository.save(b);
    }

    private void schedule(Bus b, LocalTime dep, LocalTime arr, String trip) {
        Schedule s = new Schedule();
        s.setBus(b);
        s.setDepartureTime(dep);
        s.setArrivalTime(arr);
        s.setTrip(trip);
        s.setDays("MON,TUE,WED,THU,FRI,SAT");
        scheduleRepository.save(s);
    }

    private void notification(String title, String message, Bus bus) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setBus(bus);
        notificationRepository.save(n);
    }
}
