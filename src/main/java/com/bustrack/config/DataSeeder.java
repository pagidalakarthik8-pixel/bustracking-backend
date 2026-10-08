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
        if (!enabled) {
            return;
        }

        // Older installations were created with only the first three demo buses.
        // Add the expanded fleet once without replacing any user-created data.
        if (userRepository.count() > 0) {
            if (busRepository.count() == 3 && routeRepository.count() == 3) {
                seedAdditionalFleet();
                log.info("Expanded the legacy demo fleet to 30 buses and 30 routes.");
            }
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

        Route r1 = route("Route 1 - Uppal to Anurag University", "Uppal", "Anurag University", 24.5,
                new String[]{"Uppal Ring Road", "Habsiguda", "Tarnaka", "Ghatkesar Cross", "Anurag University"},
                new int[][]{{7, 0}, {7, 15}, {7, 30}, {7, 55}, {8, 15}});
        Route r2 = route("Route 2 - Kukatpally to Anurag University", "Kukatpally", "Anurag University", 38.0,
                new String[]{"KPHB Colony", "Moosapet", "Ameerpet", "Secunderabad", "Anurag University"},
                new int[][]{{6, 30}, {6, 45}, {7, 10}, {7, 35}, {8, 20}});
        Route r3 = route("Route 3 - LB Nagar to Anurag University", "LB Nagar", "Anurag University", 31.2,
                new String[]{"Dilsukhnagar", "Nagole", "Uppal Depot", "Anurag University"},
                new int[][]{{6, 45}, {7, 0}, {7, 25}, {8, 10}});

        Bus b1 = bus("BT-01", "TS08 UA 1021", 50, d1, r1, BusStatus.ON_TIME, null);
        Bus b2 = bus("BT-02", "TS08 UB 2043", 55, d2, r2, BusStatus.DELAYED, "Running about 15 minutes late due to traffic");
        Bus b3 = bus("BT-03", "TS08 UC 3087", 45, d3, r3, BusStatus.ON_TIME, null);

        seedAdditionalFleet();

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

        notification("Welcome to BusTrack",
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

    /** Adds routes 4-30 so the demo fleet covers the main approaches to Anurag University. */
    private void seedAdditionalFleet() {
        String[] driverNames = {
                "Mahesh Yadav", "Prakash Naidu", "Srinivas Goud", "Rajesh Singh", "Anil Kumar",
                "Kiran Reddy", "Narasimha Rao", "Vijay Kumar", "Ravi Teja", "Praveen Goud",
                "Bala Krishna", "Manoj Sharma", "Sanjay Reddy", "Krishna Mohan", "Sai Kumar",
                "Harish Babu", "Gopi Nath", "Sandeep Kumar", "Raghavendra Rao", "Ashok Kumar",
                "Mohan Reddy", "Naveen Kumar", "Chaitanya Rao", "Rohit Reddy", "Dinesh Kumar",
                "Vamsi Krishna", "Srikant Reddy"
        };

        // Start point, approximate distance in km, and ordered boarding stops.
        String[][] routeData = {
                {"Ghatkesar", "12.0", "Ghatkesar|NFC Nagar|Venkatadri Township|Anurag University"},
                {"Pocharam", "8.5", "Pocharam|Infosys SEZ|Narapally|Anurag University"},
                {"Medipally", "16.0", "Medipally|Chengicherla|Narapally|Anurag University"},
                {"Boduppal", "21.0", "Boduppal|Peerzadiguda|Uppal Depot|Ghatkesar Cross"},
                {"Nacharam", "25.0", "Nacharam|Mallapur|Uppal|Ghatkesar Cross"},
                {"Habsiguda", "29.0", "Habsiguda|Tarnaka|Uppal|Ghatkesar Cross"},
                {"Tarnaka", "31.0", "Tarnaka|Habsiguda|Uppal Ring Road|Ghatkesar Cross"},
                {"Secunderabad", "36.0", "Secunderabad|Mettuguda|Tarnaka|Uppal"},
                {"Malkajgiri", "33.0", "Malkajgiri|Anandbagh|ECIL X Roads|Uppal"},
                {"ECIL", "28.0", "ECIL X Roads|Kushaiguda|Kapra|Uppal"},
                {"Kapra", "26.0", "Kapra|Kushaiguda|ECIL X Roads|Uppal"},
                {"Sainikpuri", "30.0", "Sainikpuri|AS Rao Nagar|ECIL X Roads|Uppal"},
                {"Dammaiguda", "27.0", "Dammaiguda|Nagadhevata|Keesara|Ghatkesar"},
                {"Keesara", "20.0", "Keesara|Rampally|NFC Nagar|Ghatkesar"},
                {"Cherlapally", "15.0", "Cherlapally|IDA Cherlapally|Narapally|Anurag University"},
                {"Dilsukhnagar", "34.0", "Dilsukhnagar|Kothapet|Nagole|Uppal"},
                {"Kothapet", "30.0", "Kothapet|Nagole|Uppal Depot|Ghatkesar Cross"},
                {"Vanasthalipuram", "39.0", "Vanasthalipuram|LB Nagar|Nagole|Uppal"},
                {"Hayathnagar", "43.0", "Hayathnagar|Vanasthalipuram|LB Nagar|Nagole"},
                {"Ramoji Film City", "35.0", "Ramoji Film City|Abdullapurmet|Hayathnagar|LB Nagar"},
                {"Ibrahimpatnam", "42.0", "Ibrahimpatnam|Turkayamjal|LB Nagar|Nagole"},
                {"Nagole", "25.0", "Nagole|Uppal|Uppal Depot|Ghatkesar Cross"},
                {"Ramanthapur", "27.0", "Ramanthapur|Amberpet|Habsiguda|Uppal"},
                {"Moosapet", "45.0", "Moosapet|Ameerpet|Begumpet|Secunderabad"},
                {"KPHB Colony", "49.0", "KPHB Colony|Kukatpally|Moosapet|Ameerpet"},
                {"Miyapur", "54.0", "Miyapur|JNTU|Kukatpally|Moosapet"},
                {"Shamshabad", "52.0", "Shamshabad|Aramghar|Malakpet|Dilsukhnagar"}
        };

        for (int i = 0; i < routeData.length; i++) {
            int routeNumber = i + 4;
            String[] data = routeData[i];
            String startPoint = data[0];
            Driver driver = driver(driverNames[i], "9" + String.format("%09d", 876500001L + i),
                    "TS09-20" + String.format("%02d", 10 + i) + "-" + String.format("%06d", 110001 + i));
            String[] stops = data[2].split("\\|");
            int[][] pickupTimes = pickupTimes(stops.length, LocalTime.of(6, 5).plusMinutes((i % 4) * 5));
            Route route = route("Route " + routeNumber + " - " + startPoint + " to Anurag University",
                    startPoint, "Anurag University", Double.parseDouble(data[1]), stops, pickupTimes);
            BusStatus status = i % 9 == 0 ? BusStatus.DELAYED : BusStatus.ON_TIME;
            String statusNote = status == BusStatus.DELAYED ? "Running about 10 minutes late due to traffic" : null;
            Bus bus = bus(String.format("BT-%02d", routeNumber), String.format("TS08 UD %04d", 4000 + routeNumber),
                    45 + (i % 3) * 5, driver, route, status, statusNote);
            schedule(bus, LocalTime.of(6, 15), LocalTime.of(8, 45), "MORNING");
            schedule(bus, LocalTime.of(16, 30), LocalTime.of(19, 0), "EVENING");
        }
    }

    private int[][] pickupTimes(int stopCount, LocalTime firstPickup) {
        int[][] times = new int[stopCount][2];
        for (int i = 0; i < stopCount; i++) {
            LocalTime time = firstPickup.plusMinutes(i * 15L);
            times[i][0] = time.getHour();
            times[i][1] = time.getMinute();
        }
        return times;
    }

    private void notification(String title, String message, Bus bus) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setBus(bus);
        notificationRepository.save(n);
    }
}
