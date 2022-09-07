package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.response.SubStatisticsResponse;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.ReservationStatistics;
import com.marios.gymAppDemo.model.Status;
import com.marios.gymAppDemo.model.WorkoutLessons;
import com.marios.gymAppDemo.repository.ReservationStatisticsRepository;
import com.marios.gymAppDemo.repository.ReservationsRepository;
import com.marios.gymAppDemo.repository.SubscriptionWorkoutLessonsRepository;
import com.marios.gymAppDemo.repository.WorkoutLessonsRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class StatisticsService {
    private final WorkoutLessonsRepository workoutLessonsRepository;
    private final SubscriptionWorkoutLessonsRepository subscriptionWorkoutLessonsRepository;
    private final ReservationsRepository reservationsRepository;
    private final ReservationStatisticsRepository reservationStatisticsRepository;

    // returns a List of the most subscribed lessons with active subscription
    public SubStatisticsResponse getMostSubLessons(){
        List<WorkoutLessons> workoutLessons = workoutLessonsRepository.findAll();

        Map<String,Long> countWorkouts = new HashMap<>();
        Map<String,Float> sortedPercentage = new HashMap<>();

        Long sum = 0L;
        for (WorkoutLessons workout: workoutLessons) {
            long count = subscriptionWorkoutLessonsRepository.countSubscriptionWorkoutLessonsByWorkoutLessonIdAndExpired(workout.getId(),false);
            sum +=count;
            countWorkouts.put(workout.getName(),count);
        }

        Map<String,Long> sorted = countWorkouts
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        for (Map.Entry<String,Long> entry : sorted.entrySet()) {
            sortedPercentage.put(entry.getKey(),  (entry.getValue().floatValue() / sum) * 100);
        }

        return new SubStatisticsResponse(sorted,sortedPercentage);
    }

    // returns a Map with the name of the lesson as a key, and the total income of the lesson from active subscriptions as a value
    public Map<String, Double> sumOfSubscriptionLessonCosts(){
        Map<String,Double> subCosts = new HashMap<>();
        List<WorkoutLessons> workoutLessons = workoutLessonsRepository.findAll();

        for (WorkoutLessons workout: workoutLessons) {
            Double sum =  subscriptionWorkoutLessonsRepository.sumSubscriptionWorkoutLessonsLessonCostByExpiredAndWorkoutLessonId(false,workout.getId());
            if(sum == null){
                subCosts.put(workout.getName(),0D);
            }
            else{
                subCosts.put(workout.getName(),sum);
            }

        }

        Map<String,Double> sorted = subCosts
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        return sorted;
    }

    // schedule for every day at 23:59
    @Scheduled(cron = "0 59 23 * * *")
    //save to ReservationsStatistics the amount of reservations that were made at the end of each day for every workout
    public void setDataForMostPopularLessonsByDay(){
        LocalDate today = LocalDate.now();
        List<WorkoutLessons> workoutLessons = workoutLessonsRepository.findAll();

        for (WorkoutLessons workout:workoutLessons) {
            // we only need the confirmed reservations at the end of the day
            long count = reservationsRepository.countReservationsByStatusAndDateAndLessonAvailability_WorkoutLessons_Id(Status.CONFIRMED,today,workout.getId());
            //create and save reservations statistics
            reservationStatisticsRepository.save(new ReservationStatistics(today,today.getYear(),today.getMonthValue(),today.getDayOfWeek().toString(),count,workout.getName()));
        }
    }

    // returns a Map with the name of the lesson as a key, and the amount of reservations of that lesson, that were made that day as a value
    public Map<String, Long> getMostPopularLessonByDate(LocalDate date){
        Map<String,Long> countWorkouts = new HashMap<>();

        List<ReservationStatistics> reservationStatistics = reservationStatisticsRepository.findByResDate(date).
                orElseThrow(() -> new CustomNotFoundException("no_res_for_day"));

        if(reservationStatistics.isEmpty()){
            throw new CustomNotFoundException("no_res_for_day");
        }

        for (ReservationStatistics res:reservationStatistics) {
            countWorkouts.put(res.getWorkoutName(),res.getCount());
        }

        return countWorkouts;
    }

    // returns a Map with the name of the lesson as a key, and the amount of reservations of the lesson
    // that were made that month of that year as a value
    public Map<String, Long> getMostPopularLessonsByYearAndMonth(int year, int month){
        List<WorkoutLessons> workoutLessons = workoutLessonsRepository.findAll();

        Map<String,Long> countWorkouts = new HashMap<>();
        for (WorkoutLessons workout:workoutLessons) {
            Long totalCount = reservationStatisticsRepository.sumAllByYearAndMonthAndWorkoutName(year, month, workout.getName()).orElseThrow(() -> new CustomNotFoundException("no_res_for_month"));
            countWorkouts.put(workout.getName(),totalCount);
        }
        return countWorkouts;
    }

    // returns a Map with the hour as a key, and the amount of reservations that were made at that hour as a value
    public Map<String, Long> getPopularHoursOfDay(LocalDate date){
        Map<String,Long> countHour = new TreeMap<>();

        for (int i = 1; i <= 23; i++) {
            String hour;

            if(i <= 9){
                hour = String.format("%02d:00", i);
            }
            else{
                hour = String.format("%d:00", i);
            }

            long count = reservationsRepository.countReservationsByStatusAndDateAndHour(Status.CONFIRMED,date,hour);
            countHour.put(hour,count);
        }

        return countHour;
    }
}
