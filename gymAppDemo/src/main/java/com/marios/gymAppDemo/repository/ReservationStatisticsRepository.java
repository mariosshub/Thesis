package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.ReservationStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationStatisticsRepository extends JpaRepository<ReservationStatistics,Long> {

    Optional<List<ReservationStatistics>> findByResDate(LocalDate resDate);

    Optional<List<ReservationStatistics>> findByYearAndMonth(Integer year, Integer month);

    @Query("select sum(r.count) FROM ReservationStatistics r where r.year = ?1 and r.month = ?2 and r.workoutName =?3")
    Optional<Long> sumAllByYearAndMonthAndWorkoutName(Integer year, Integer month, String workoutName);
}
