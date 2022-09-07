package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.request.CountForReservationsRequest;
import com.marios.gymAppDemo.request.MakeReservationRequest;
import com.marios.gymAppDemo.response.ReservationsResponse;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.*;
import com.marios.gymAppDemo.repository.LessonAvailabilityRepository;
import com.marios.gymAppDemo.repository.ReservationsRepository;
import com.marios.gymAppDemo.repository.SubscriptionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationsService {
    private final ReservationsRepository reservationsRepository;
    private final SubscriptionsRepository subscriptionsRepository;
    private final LessonAvailabilityRepository lessonAvailabilityRepository;

    @Autowired
    public ReservationsService(ReservationsRepository reservationsRepository, SubscriptionsRepository subscriptionsRepository, LessonAvailabilityRepository lessonAvailabilityRepository){
        this.reservationsRepository = reservationsRepository;
        this.subscriptionsRepository = subscriptionsRepository;
        this.lessonAvailabilityRepository = lessonAvailabilityRepository;
    }

    // creates a reservation for a workout lesson and returns true if successful
    public Boolean makeReservation(MakeReservationRequest reservationRequest){
        Subscription subscription = subscriptionsRepository.findByCustomerUsername(reservationRequest.getCustomersUsername()).orElseThrow(() -> new CustomNotFoundException("sub_not_found"));
        LessonAvailability lessonAvailability = lessonAvailabilityRepository.findById(reservationRequest.getAvailabilityId()).orElseThrow(() -> new CustomNotFoundException("availability_not_found"));
        // get all booked reservations of the customer
        Optional<List<Reservations>> existingBookedReservations = reservationsRepository.findAllByStatusAndSubscriptionId(Status.BOOKED, subscription.getId());
        if(existingBookedReservations.isPresent()){
            List<Reservations> bookedReservations = existingBookedReservations.get();

            for (Reservations bookedReservation: bookedReservations) {
                String date = bookedReservation.getLessonAvailability().getDate();

                int existingResHour = Integer.parseInt(bookedReservation.getHour().split(":")[0]);

                int resHour = Integer.parseInt(reservationRequest.getHourOfReservation().split(":")[0]);

                // check if customer tries to make a reservation that matches the date and time of an existing one
                if(existingResHour == resHour && date.equals(lessonAvailability.getDate())){
                    throw new CustomNotFoundException("booked_lessons");
                }
            }
        }
        boolean lessonExpired = false;
        boolean makeReservation = false;

        //check if users subscription has the workout lesson
        for(SubscriptionWorkoutLessons usersLessons : subscription.getSubscriptionWorkoutLessons()){
            if(usersLessons.getWorkoutLesson().getId().equals(lessonAvailability.getWorkoutLessons().getId())){
                if(!usersLessons.isExpired()){
                    makeReservation = true;
                }
                else{
                    lessonExpired = true;
                }
                break;
            }
        }

        if(makeReservation){
            int resHour = Integer.parseInt(reservationRequest.getHourOfReservation().split(":")[0]);
            int startingHour = Integer.parseInt(lessonAvailability.getStartingHour().split(":")[0]);
            int endingHour = Integer.parseInt(lessonAvailability.getEndHour().split(":")[0]);

            if(resHour >= startingHour && resHour <endingHour){
                Reservations reservation = new Reservations(lessonAvailability.getDate(),LocalDate.now(),reservationRequest.getHourOfReservation(),Status.BOOKED,subscription,lessonAvailability);
                reservationsRepository.save(reservation);
                return true;
            }
            else{
                throw new CustomNotFoundException("res_available_hour");
            }
        }
        else if(lessonExpired){
            throw new CustomNotFoundException("sub_lesson_exp");
        }
        else{
            throw new CustomNotFoundException("customer_not_sub");
        }
    }

    // return the number of reservations of a workout lesson by date, booked status, and hour
    public long countReservations(CountForReservationsRequest countReq){
        return reservationsRepository.countReservationsByLessonAvailabilityIdAndDayOfWeekAndStatusAndHour(countReq.getAvailabilityId(),countReq.getDayOfWeek(),Status.BOOKED,countReq.getHour());
    }

    // returns a list of reservations that were made by Customer's username that are ordered by date
    public List<ReservationsResponse> findAllReservationsByUsernameAndOrderByDate(String username){
        List<Reservations> reservations = reservationsRepository.findAllBySubscriptionCustomerUsernameOrderByDateDesc(username).orElseThrow(() -> new CustomNotFoundException("res_not_found"));

        List<ReservationsResponse> resResponse = new ArrayList<>();
        for (Reservations res:reservations) {
            resResponse.add(new ReservationsResponse(res.getId(),res.getDayOfWeek(),res.getDate().toString(),res.getHour(),res.getStatus(),res.getLessonAvailability().getWorkoutLessons().getName()));
        }
        return resResponse;
    }

    // cancel a reservation by id
    public Boolean cancelReservation(Long id){
        Reservations reservation = reservationsRepository.findById(id).orElseThrow(() -> new CustomNotFoundException("res_not_found"));
        switch (reservation.getStatus()){
            case CANCELLED:
                throw new CustomNotFoundException("res_canceled");
            case CONFIRMED:
                throw new CustomNotFoundException("res_made");
            case BOOKED:
                reservation.setStatus(Status.CANCELLED);
                break;
        }
        reservationsRepository.save(reservation);
        return true;
    }

    // confirm booked reservations
    // every hour from 1 through 23
    @Scheduled(cron = "0 0 1-23 * * *")
    public void searchAndConfirmRes(){
        int hour = LocalDateTime.now().getHour();
        String curHour;
        if(hour <= 9){
            curHour = String.format("%02d:00", hour);
        }
        else{
            curHour = String.format("%d:00", hour);
        }
        System.out.println(curHour);
        System.out.println(LocalDateTime.now());
        Optional<List<Reservations>> reservations = reservationsRepository.findAllByStatusAndHour(Status.BOOKED,curHour);

        if(reservations.isPresent()){
            for (Reservations reservation: reservations.get()) {
                reservation.setStatus(Status.CONFIRMED);
                reservationsRepository.save(reservation);
            }
        }
    }

}
