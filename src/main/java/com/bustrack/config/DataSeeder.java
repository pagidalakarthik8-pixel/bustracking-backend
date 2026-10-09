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

import java.time.Instant;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Seeds administrative accounts and a full 30-bus, 30-route fleet connecting
 * major Hyderabad and Rangareddy localities directly to Anurag University, Ghatkesar.
 */
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

    private static record FleetSeedItem(
            String busNumber,
            String regNumber,
            int capacity,
            String driverName,
            String driverPhone,
            String driverLicense,
            String routeName,
            String startPoint,
            double distanceKm,
            String[] stops,
            int[][] pickupTimes,
            BusStatus status,
            String statusNote,
            double latitude,
            double longitude,
            LocalTime morningDep,
            LocalTime morningArr,
            LocalTime eveningDep,
            LocalTime eveningArr
    ) {}

    private static final FleetSeedItem[] FLEET_ITEMS = new FleetSeedItem[]{
            new FleetSeedItem("BT-01", "TS08 UA 1021", 50, "Ramesh Kumar", "9876500001", "TS09-2015-001122",
                    "Route 1 - Uppal to Anurag University", "Uppal", 24.5,
                    new String[]{"Uppal Ring Road", "Habsiguda", "Tarnaka", "Ghatkesar Cross", "Anurag University"},
                    new int[][]{{7, 0}, {7, 15}, {7, 30}, {7, 55}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.4147, 78.5522,
                    LocalTime.of(7, 0), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 45)),

            new FleetSeedItem("BT-02", "TS08 UB 2043", 55, "Suresh Reddy", "9876500002", "TS09-2012-003344",
                    "Route 2 - Kukatpally to Anurag University", "Kukatpally", 38.0,
                    new String[]{"KPHB Colony", "Moosapet", "Ameerpet", "Secunderabad", "Anurag University"},
                    new int[][]{{6, 30}, {6, 45}, {7, 10}, {7, 35}, {8, 20}},
                    BusStatus.DELAYED, "Running about 15 minutes late due to traffic", 17.4375, 78.4483,
                    LocalTime.of(6, 30), LocalTime.of(8, 30), LocalTime.of(16, 30), LocalTime.of(19, 0)),

            new FleetSeedItem("BT-03", "TS08 UC 3087", 45, "Venkat Rao", "9876500003", "TS09-2018-005566",
                    "Route 3 - LB Nagar to Anurag University", "LB Nagar", 31.2,
                    new String[]{"Dilsukhnagar", "Kothapet", "Nagole", "Uppal Depot", "Anurag University"},
                    new int[][]{{6, 45}, {7, 0}, {7, 25}, {7, 50}, {8, 10}},
                    BusStatus.ON_TIME, null, 17.3556, 78.5516,
                    LocalTime.of(6, 45), LocalTime.of(8, 15), LocalTime.of(16, 30), LocalTime.of(18, 45)),

            new FleetSeedItem("BT-04", "TS08 UD 4004", 50, "Mahesh Yadav", "9876500004", "TS09-2019-004411",
                    "Route 4 - Ghatkesar to Anurag University", "Ghatkesar", 12.0,
                    new String[]{"Ghatkesar Town", "NFC Nagar", "Venkatadri Township", "Anurag University"},
                    new int[][]{{7, 30}, {7, 45}, {8, 0}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.4475, 78.6750,
                    LocalTime.of(7, 30), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(17, 30)),

            new FleetSeedItem("BT-05", "TS08 UD 4005", 52, "Prakash Naidu", "9876500005", "TS09-2017-005522",
                    "Route 5 - Pocharam to Anurag University", "Pocharam", 14.5,
                    new String[]{"Pocharam", "Infosys SEZ", "Singapore Township", "Narapally", "Anurag University"},
                    new int[][]{{7, 20}, {7, 35}, {7, 50}, {8, 5}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.4520, 78.6430,
                    LocalTime.of(7, 20), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(17, 45)),

            new FleetSeedItem("BT-06", "TS08 UD 4006", 50, "Srinivas Goud", "9876500006", "TS09-2016-006633",
                    "Route 6 - Medipally to Anurag University", "Medipally", 16.0,
                    new String[]{"Medipally", "Canara Nagar", "Chengicherla X Road", "Narapally", "Anurag University"},
                    new int[][]{{7, 15}, {7, 30}, {7, 45}, {8, 0}, {8, 18}},
                    BusStatus.ON_TIME, null, 17.4280, 78.6010,
                    LocalTime.of(7, 15), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 0)),

            new FleetSeedItem("BT-07", "TS08 UD 4007", 55, "Rajesh Singh", "9876500007", "TS09-2018-007744",
                    "Route 7 - Boduppal to Anurag University", "Boduppal", 21.0,
                    new String[]{"Boduppal", "Ambedkar Nagar", "Peerzadiguda", "Ghatkesar Cross", "Anurag University"},
                    new int[][]{{7, 5}, {7, 20}, {7, 40}, {8, 0}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.4110, 78.5820,
                    LocalTime.of(7, 5), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 15)),

            new FleetSeedItem("BT-08", "TS08 UD 4008", 48, "Anil Kumar", "9876500008", "TS09-2014-008855",
                    "Route 8 - Nacharam to Anurag University", "Nacharam", 25.0,
                    new String[]{"Nacharam Industrial Area", "Mallapur", "Uppal Ring Road", "Ghatkesar Cross", "Anurag University"},
                    new int[][]{{7, 0}, {7, 15}, {7, 35}, {8, 0}, {8, 22}},
                    BusStatus.ON_TIME, null, 17.4320, 78.5610,
                    LocalTime.of(7, 0), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-09", "TS08 UD 4009", 50, "Kiran Reddy", "9876500009", "TS09-2016-009966",
                    "Route 9 - Habsiguda to Anurag University", "Habsiguda", 27.0,
                    new String[]{"Habsiguda Street 8", "Street 1", "Uppal X Roads", "Narapally", "Anurag University"},
                    new int[][]{{6, 55}, {7, 10}, {7, 30}, {7, 55}, {8, 18}},
                    BusStatus.DELAYED, "Heavy traffic near Uppal flyover", 17.4080, 78.5440,
                    LocalTime.of(6, 55), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-10", "TS08 UD 4010", 52, "Narasimha Rao", "9876500010", "TS09-2013-010077",
                    "Route 10 - Tarnaka to Anurag University", "Tarnaka", 29.5,
                    new String[]{"Tarnaka Metro", "Lalaguda", "Habsiguda", "Uppal", "Anurag University"},
                    new int[][]{{6, 50}, {7, 5}, {7, 25}, {7, 50}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.4270, 78.5320,
                    LocalTime.of(6, 50), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-11", "TS08 UD 4011", 55, "Vijay Kumar", "9876500011", "TS09-2015-011188",
                    "Route 11 - Secunderabad to Anurag University", "Secunderabad", 33.0,
                    new String[]{"Secunderabad Station", "Mettuguda", "Tarnaka", "Uppal", "Anurag University"},
                    new int[][]{{6, 40}, {7, 0}, {7, 20}, {7, 45}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.4390, 78.5030,
                    LocalTime.of(6, 40), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 45)),

            new FleetSeedItem("BT-12", "TS08 UD 4012", 50, "Ravi Teja", "9876500012", "TS09-2017-012299",
                    "Route 12 - Malkajgiri to Anurag University", "Malkajgiri", 31.0,
                    new String[]{"Malkajgiri", "Anandbagh", "Neredmet X Roads", "ECIL", "Anurag University"},
                    new int[][]{{6, 45}, {7, 0}, {7, 20}, {7, 45}, {8, 18}},
                    BusStatus.ON_TIME, null, 17.4530, 78.5280,
                    LocalTime.of(6, 45), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 45)),

            new FleetSeedItem("BT-13", "TS08 UD 4013", 54, "Praveen Goud", "9876500013", "TS09-2019-013311",
                    "Route 13 - ECIL to Anurag University", "ECIL", 26.0,
                    new String[]{"ECIL X Roads", "Kushaiguda", "Chakripuram", "Cherlapally", "Anurag University"},
                    new int[][]{{7, 0}, {7, 15}, {7, 35}, {7, 55}, {8, 18}},
                    BusStatus.ON_TIME, null, 17.4680, 78.5580,
                    LocalTime.of(7, 0), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-14", "TS08 UD 4014", 50, "Bala Krishna", "9876500014", "TS09-2014-014422",
                    "Route 14 - Kapra to Anurag University", "Kapra", 25.5,
                    new String[]{"Kapra Lake", "Saket", "Kushaiguda", "Ghatkesar", "Anurag University"},
                    new int[][]{{7, 5}, {7, 20}, {7, 40}, {8, 0}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.4880, 78.5620,
                    LocalTime.of(7, 5), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-15", "TS08 UD 4015", 52, "Manoj Sharma", "9876500015", "TS09-2016-015533",
                    "Route 15 - Sainikpuri to Anurag University", "Sainikpuri", 29.0,
                    new String[]{"Sainikpuri", "Vayupuri", "AS Rao Nagar", "ECIL", "Anurag University"},
                    new int[][]{{6, 50}, {7, 5}, {7, 25}, {7, 50}, {8, 16}},
                    BusStatus.ON_TIME, null, 17.4920, 78.5480,
                    LocalTime.of(6, 50), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 40)),

            new FleetSeedItem("BT-16", "TS08 UD 4016", 48, "Sanjay Reddy", "9876500016", "TS09-2018-016644",
                    "Route 16 - Dammaiguda to Anurag University", "Dammaiguda", 22.0,
                    new String[]{"Dammaiguda", "Nagaram", "Rampally X Road", "Ghatkesar", "Anurag University"},
                    new int[][]{{7, 10}, {7, 25}, {7, 45}, {8, 5}, {8, 22}},
                    BusStatus.ON_TIME, null, 17.4990, 78.5830,
                    LocalTime.of(7, 10), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 15)),

            new FleetSeedItem("BT-17", "TS08 UD 4017", 50, "Krishna Mohan", "9876500017", "TS09-2015-017755",
                    "Route 17 - Keesara to Anurag University", "Keesara", 19.5,
                    new String[]{"Keesara Gutta X Road", "Rampally", "Bogaram", "Ghatkesar", "Anurag University"},
                    new int[][]{{7, 15}, {7, 30}, {7, 48}, {8, 5}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.5180, 78.6520,
                    LocalTime.of(7, 15), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 0)),

            new FleetSeedItem("BT-18", "TS08 UD 4018", 52, "Sai Kumar", "9876500018", "TS09-2020-018866",
                    "Route 18 - Cherlapally to Anurag University", "Cherlapally", 15.0,
                    new String[]{"Cherlapally Railway Station", "Bharat Nagar", "Narapally", "Anurag University"},
                    new int[][]{{7, 25}, {7, 40}, {7, 55}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.4610, 78.6010,
                    LocalTime.of(7, 25), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(17, 45)),

            new FleetSeedItem("BT-19", "TS08 UD 4019", 55, "Harish Babu", "9876500019", "TS09-2013-019977",
                    "Route 19 - Dilsukhnagar to Anurag University", "Dilsukhnagar", 32.0,
                    new String[]{"Dilsukhnagar Bus Depot", "Chaitanyapuri", "Kothapet", "Uppal", "Anurag University"},
                    new int[][]{{6, 45}, {7, 0}, {7, 20}, {7, 48}, {8, 15}},
                    BusStatus.DELAYED, "Delay due to metro construction near Nagole", 17.3680, 78.5280,
                    LocalTime.of(6, 45), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 50)),

            new FleetSeedItem("BT-20", "TS08 UD 4020", 50, "Gopi Nath", "9876500020", "TS09-2017-020088",
                    "Route 20 - Kothapet to Anurag University", "Kothapet", 28.0,
                    new String[]{"Kothapet Fruit Market", "HUDA Complex", "Nagole", "Uppal", "Anurag University"},
                    new int[][]{{6, 55}, {7, 10}, {7, 30}, {7, 52}, {8, 18}},
                    BusStatus.ON_TIME, null, 17.3730, 78.5440,
                    LocalTime.of(6, 55), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-21", "TS08 UD 4021", 55, "Sandeep Kumar", "9876500021", "TS09-2016-021199",
                    "Route 21 - Vanasthalipuram to Anurag University", "Vanasthalipuram", 36.0,
                    new String[]{"Vanasthalipuram", "Sushma", "Auto Nagar", "LB Nagar", "Anurag University"},
                    new int[][]{{6, 35}, {6, 50}, {7, 15}, {7, 42}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.3320, 78.5670,
                    LocalTime.of(6, 35), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 50)),

            new FleetSeedItem("BT-22", "TS08 UD 4022", 50, "Raghavendra Rao", "9876500022", "TS09-2014-022211",
                    "Route 22 - Hayathnagar to Anurag University", "Hayathnagar", 34.0,
                    new String[]{"Hayathnagar Depot", "Word & Deed", "Pedda Amberpet", "Ghatkesar ORR", "Anurag University"},
                    new int[][]{{6, 40}, {6, 55}, {7, 18}, {7, 45}, {8, 12}},
                    BusStatus.ON_TIME, null, 17.3270, 78.6020,
                    LocalTime.of(6, 40), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 45)),

            new FleetSeedItem("BT-23", "TS08 UD 4023", 48, "Ashok Kumar", "9876500023", "TS09-2018-023322",
                    "Route 23 - Ramoji Film City to Anurag University", "Ramoji Film City", 32.5,
                    new String[]{"RFC Main Gate", "Abdullapurmet", "Kawadipally", "Ghatkesar", "Anurag University"},
                    new int[][]{{6, 45}, {7, 0}, {7, 22}, {7, 48}, {8, 15}},
                    BusStatus.ON_TIME, null, 17.3110, 78.6810,
                    LocalTime.of(6, 45), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 30)),

            new FleetSeedItem("BT-24", "TS08 UD 4024", 55, "Mohan Reddy", "9876500024", "TS09-2015-024433",
                    "Route 24 - Ibrahimpatnam to Anurag University", "Ibrahimpatnam", 42.0,
                    new String[]{"Ibrahimpatnam", "Mangalpalli", "Bongloor ORR", "Ghatkesar", "Anurag University"},
                    new int[][]{{6, 25}, {6, 45}, {7, 10}, {7, 45}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.1950, 78.6480,
                    LocalTime.of(6, 25), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(19, 0)),

            new FleetSeedItem("BT-25", "TS08 UD 4025", 50, "Naveen Kumar", "9876500025", "TS09-2019-025544",
                    "Route 25 - Nagole to Anurag University", "Nagole", 23.5,
                    new String[]{"Nagole Metro", "Anand Nagar", "Uppal Ring Road", "Narapally", "Anurag University"},
                    new int[][]{{7, 5}, {7, 20}, {7, 40}, {8, 0}, {8, 20}},
                    BusStatus.ON_TIME, null, 17.3780, 78.5600,
                    LocalTime.of(7, 5), LocalTime.of(8, 25), LocalTime.of(16, 30), LocalTime.of(18, 15)),

            new FleetSeedItem("BT-26", "TS08 UD 4026", 50, "Chaitanya Rao", "9876500026", "TS09-2017-026655",
                    "Route 26 - Ramanthapur to Anurag University", "Ramanthapur", 26.0,
                    new String[]{"Ramanthapur", "TV Studio", "Amberpet", "Uppal", "Anurag University"},
                    new int[][]{{7, 0}, {7, 15}, {7, 35}, {7, 55}, {8, 18}},
                    BusStatus.ON_TIME, null, 17.3910, 78.5300,
                    LocalTime.of(7, 0), LocalTime.of(8, 20), LocalTime.of(16, 30), LocalTime.of(18, 20)),

            new FleetSeedItem("BT-27", "TS08 UD 4027", 55, "Rohit Reddy", "9876500027", "TS09-2016-027766",
                    "Route 27 - Moosapet to Anurag University", "Moosapet", 44.0,
                    new String[]{"Moosapet Metro", "Bharat Nagar", "Sanath Nagar", "Secunderabad", "Anurag University"},
                    new int[][]{{6, 20}, {6, 35}, {7, 0}, {7, 35}, {8, 22}},
                    BusStatus.ON_TIME, null, 17.4650, 78.4310,
                    LocalTime.of(6, 20), LocalTime.of(8, 30), LocalTime.of(16, 30), LocalTime.of(19, 0)),

            new FleetSeedItem("BT-28", "TS08 UD 4028", 55, "Dinesh Kumar", "9876500028", "TS09-2014-028877",
                    "Route 28 - KPHB Colony to Anurag University", "KPHB Colony", 48.0,
                    new String[]{"KPHB 1st Road", "JNTU X Road", "Balanagar", "Tarnaka", "Anurag University"},
                    new int[][]{{6, 15}, {6, 30}, {7, 0}, {7, 40}, {8, 25}},
                    BusStatus.DELAYED, "Heavy traffic on Outer Ring Road", 17.4930, 78.3990,
                    LocalTime.of(6, 15), LocalTime.of(8, 30), LocalTime.of(16, 30), LocalTime.of(19, 15)),

            new FleetSeedItem("BT-29", "TS08 UD 4029", 55, "Vamsi Krishna", "9876500029", "TS09-2018-029988",
                    "Route 29 - Miyapur to Anurag University", "Miyapur", 53.0,
                    new String[]{"Miyapur X Roads", "Allwyn Colony", "Kukatpally", "Secunderabad", "Anurag University"},
                    new int[][]{{6, 10}, {6, 25}, {6, 55}, {7, 35}, {8, 28}},
                    BusStatus.ON_TIME, null, 17.4970, 78.3580,
                    LocalTime.of(6, 10), LocalTime.of(8, 35), LocalTime.of(16, 30), LocalTime.of(19, 20)),

            new FleetSeedItem("BT-30", "TS08 UD 4030", 55, "Srikant Reddy", "9876500030", "TS09-2015-030099",
                    "Route 30 - Shamshabad to Anurag University", "Shamshabad", 51.0,
                    new String[]{"Shamshabad Bus Stop", "Aramghar", "Chandrayangutta", "LB Nagar", "Anurag University"},
                    new int[][]{{6, 15}, {6, 35}, {7, 5}, {7, 40}, {8, 25}},
                    BusStatus.ON_TIME, null, 17.2510, 78.4280,
                    LocalTime.of(6, 15), LocalTime.of(8, 35), LocalTime.of(16, 30), LocalTime.of(19, 20))
    };

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }

        ensureUsers();
        seedFullFleet();
    }

    private void ensureUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setName("Transport Admin");
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            User student = new User();
            student.setName("Demo Student");
            student.setEmail("student@bustrack.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole(Role.STUDENT);
            student.setRollNumber("24EG110000");
            student.setPhone("9000000000");
            student.setBoardingStop("Habsiguda");
            userRepository.save(student);

            notification("Welcome to Anurag University Bus Tracking",
                    "Check your bus timings, routes, and live GPS positions here.", null);
            log.info("Initialized default admin and student users.");
        }
    }

    private void seedFullFleet() {
        if (busRepository.count() >= 30 && routeRepository.count() >= 30) {
            log.info("Fleet already has {} buses and {} routes. All 30 routes active.",
                    busRepository.count(), routeRepository.count());
            return;
        }

        log.info("Seeding / completing the 30 buses and 30 routes to Anurag University...");

        for (FleetSeedItem item : FLEET_ITEMS) {
            if (busRepository.existsByBusNumber(item.busNumber())) {
                continue;
            }

            // 1. Driver
            Driver driver = driver(item.driverName(), item.driverPhone(), item.driverLicense());

            // 2. Route & Stops
            Route route = route(item.routeName(), item.startPoint(), "Anurag University",
                    item.distanceKm(), item.stops(), item.pickupTimes());

            // 3. Bus
            Bus bus = bus(item.busNumber(), item.regNumber(), item.capacity(),
                    driver, route, item.status(), item.statusNote());

            // 4. GPS Telemetry
            gps(bus, item.latitude(), item.longitude());

            // 5. Schedules (Morning to Anurag University, Evening departure from campus)
            schedule(bus, item.morningDep(), item.morningArr(), "MORNING");
            schedule(bus, item.eveningDep(), item.eveningArr(), "EVENING");
        }

        // Link demo student to bus BT-01 if not already linked
        Optional<User> studentOpt = userRepository.findByEmail("student@bustrack.com");
        if (studentOpt.isPresent()) {
            User student = studentOpt.get();
            if (student.getBus() == null) {
                busRepository.findAllByOrderByBusNumberAsc().stream()
                        .filter(b -> "BT-01".equals(b.getBusNumber()))
                        .findFirst()
                        .ifPresent(b -> {
                            student.setBus(b);
                            userRepository.save(student);
                        });
            }
        }

        log.info("Successfully seeded 30 buses and 30 routes for Anurag University.");
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
            int hour = (i < times.length) ? times[i][0] : 7;
            int minute = (i < times.length) ? times[i][1] : 0;
            s.setPickupTime(LocalTime.of(hour, minute));
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

    private void gps(Bus bus, double latitude, double longitude) {
        bus.setLatitude(latitude);
        bus.setLongitude(longitude);
        bus.setLocationUpdatedAt(Instant.now());
        busRepository.save(bus);
    }

    private void notification(String title, String message, Bus bus) {
        Notification n = new Notification();
        n.setTitle(title);
        n.setMessage(message);
        n.setBus(bus);
        notificationRepository.save(n);
    }
}
