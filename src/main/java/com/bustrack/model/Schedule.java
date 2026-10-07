package com.bustrack.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;

    /** MORNING (home to college) or EVENING (college to home). */
    @Column(nullable = false)
    private String trip;

    /** Comma separated, e.g. "MON,TUE,WED,THU,FRI,SAT". */
    @Column(nullable = false)
    private String days;
}
