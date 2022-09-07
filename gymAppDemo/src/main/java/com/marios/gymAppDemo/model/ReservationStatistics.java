package com.marios.gymAppDemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate resDate;
    private Integer year;
    private Integer month;
    private String dayOfWeek;
    private Long count;
    private String workoutName;

    public ReservationStatistics(LocalDate resDate, int year, int month, String dayOfWeek, long count, String workoutName) {
        this.resDate = resDate;
        this.year = year;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.count = count;
        this.workoutName = workoutName;
    }
}
