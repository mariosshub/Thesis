package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.Reservations;
import com.marios.gymAppDemo.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationsRepository extends JpaRepository<Reservations, Long> {
    Optional<List<Reservations>> findAllByStatusAndSubscriptionId (Status status, Long id);
    Optional<List<Reservations>> findAllBySubscriptionCustomerUsernameOrderByDateDesc (String username);
    long countReservationsByLessonAvailabilityIdAndDayOfWeekAndStatusAndHour (Long availabilityId, String dayOfWeek, Status status, String hour);
    Optional<List<Reservations>> findAllByStatusAndHour (Status status, String hour);


    long countReservationsByStatusAndDateAndLessonAvailability_WorkoutLessons_Id(Status status, LocalDate date, Long workoutId);

    long countReservationsByStatusAndDateAndHour(Status status,LocalDate date,String hour);

}
