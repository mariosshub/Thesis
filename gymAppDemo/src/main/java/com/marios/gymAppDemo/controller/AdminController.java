package com.marios.gymAppDemo.controller;

import com.marios.gymAppDemo.request.*;
import com.marios.gymAppDemo.response.SubStatisticsResponse;
import com.marios.gymAppDemo.model.*;
import com.marios.gymAppDemo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {
    private final CustomerService service;
    private final WorkoutLessonsService workoutLessonsService;
    private final LessonAvailabilityService lessonAvailabilityService;
    private final SubscriptionsService subService;
    private static final String SCHEDULED_TASKS = "scheduledTasks";
    private final ScheduledAnnotationBeanPostProcessor postProcessor;
    private final ReservationsService reservationsService;
    private final StatisticsService statisticsService;

    // shows profile details
    @GetMapping("/showProfile")
    public ResponseEntity<Customer> showProfile(Principal principal){
        Customer customer =  service.findCustomerByUsername(principal.getName());
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    // updates profile details
    @PutMapping("/updateProfile")
    public ResponseEntity<Customer> updateAdmin(@RequestBody UpdateProfileRequest profileRequest, Principal principal){
        Customer updateCustomer = service.updateCustomer(profileRequest, principal.getName());
        return new ResponseEntity<>(updateCustomer,HttpStatus.CREATED);
    }

    // updates photo url
    @PutMapping("/updatePhoto/{imgUrl}")
    public ResponseEntity<Boolean> updatePhoto(@PathVariable String imgUrl, Principal principal){
        Boolean flag = service.updatePhoto(imgUrl, principal.getName());
        return new ResponseEntity<>(flag,HttpStatus.CREATED);
    }

    // finds all workout lessons that exist
    @GetMapping("/findAllLessons")
    public ResponseEntity<List<WorkoutLessons>> findAllLessons(){
        List<WorkoutLessons> workoutLessons = workoutLessonsService.findAllWorkoutLessons();
        return new ResponseEntity<>(workoutLessons,HttpStatus.OK);
    }

    // adds a new workout lesson
    @PostMapping("/addLesson")
    public ResponseEntity<WorkoutLessons> addWorkoutLesson(@RequestBody WorkoutLessonRequest workoutLesson){
        WorkoutLessons newLesson = workoutLessonsService.addWorkoutLesson(workoutLesson);
        return new ResponseEntity<>(newLesson,HttpStatus.CREATED);
    }

    // updates workout lessons name description or cost
    @PutMapping("/updateLesson")
    public ResponseEntity<WorkoutLessons> updateLesson(@RequestBody WorkoutLessonRequest workoutLesson){
        WorkoutLessons newWorkoutLesson = workoutLessonsService.updateWorkoutLesson(workoutLesson);
        return new ResponseEntity<>(newWorkoutLesson,HttpStatus.CREATED);
    }

    // checks if workout lessons have availabilities
    @GetMapping("/checkLessonsAvailabilitiesIfEmpty")
    public ResponseEntity<Boolean> checkLessonsAvailabilities(){
        boolean flag = workoutLessonsService.checkWorkoutLessonsAvailabilitiesIfEmpty();
        return new ResponseEntity<>(flag,HttpStatus.OK);
    }

    // adds a new availability for a workout lesson
    @PostMapping("/addLessonAvailability")
    public ResponseEntity<WorkoutLessons> addLessonAvailability(@RequestBody LessonAvailabilityRequest lessonAvailabilityRequest){
        WorkoutLessons lessons = workoutLessonsService.addLessonAvailability(lessonAvailabilityRequest);
        return new ResponseEntity<>(lessons, HttpStatus.CREATED);
    }

    // deletes availability of a workout lesson
    @DeleteMapping("deleteWorkoutLessonAvailability/{id}")
    public ResponseEntity<WorkoutLessons> deleteLessonAvailability(@PathVariable Long id){
        WorkoutLessons lesson = lessonAvailabilityService.deleteLessonAvailability(id);
        return new ResponseEntity<>(lesson,HttpStatus.OK);
    }

    // finds the subscription of a customer by email
    @GetMapping("/findSubByEmail/{email}")
    public ResponseEntity<Subscription> getSubByEmail(@PathVariable String email){
        Subscription subscription = subService.getSubByEmail(email);
        return new ResponseEntity<>(subscription,HttpStatus.OK);

    }

    // adds subscription to customer
    @PostMapping("/addSubToCustomer")
    public ResponseEntity<Subscription> addSubToCustomer(@RequestBody AddSubRequest addSubRequest){
        Subscription newSub = subService.addSubToCustomer(addSubRequest);
        return new ResponseEntity<>(newSub,HttpStatus.CREATED);
    }

    // updates the lessons subscription of a customer when is expired
    @PutMapping("/updateSub")
    public ResponseEntity<Subscription> editSub(@RequestBody EditSubRequest editSubRequest){
        Subscription subscription = subService.updateSub(editSubRequest);
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }

    // returns statistics about the subscriptions that a lesson has
    @GetMapping("/getMostSubLessons")
    public ResponseEntity<SubStatisticsResponse> getMostSubLessons (){
        SubStatisticsResponse response = statisticsService.getMostSubLessons();
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    // returns statistics about the lessons income from subscriptions
    @GetMapping("/getSumLessonCost")
    public ResponseEntity<Map<String,Double>> getSum(){
        Map<String, Double> list = statisticsService.sumOfSubscriptionLessonCosts();
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    // returns statistics about the amount of reservations, that each workout lesson had that day
    // date has to be at format yyyy-MM-dd
    @GetMapping("/getMostPopularLessonsByDate/{day}")
    public ResponseEntity<Map<String,Long>> getMostPopularLessonsByDay(@PathVariable String day){
        Map<String, Long> list = statisticsService.getMostPopularLessonByDate(LocalDate.parse(day));
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    // returns statistics about the amount of reservations, that each workout lesson had at that month of that year
    @GetMapping("/getMostPopularLessonsByMonth/{year}/{month}")
    public ResponseEntity<Map<String,Long>>  getMostPopularLessonsByMonth(@PathVariable int year, @PathVariable int month){
        Map<String, Long> list = statisticsService.getMostPopularLessonsByYearAndMonth(year, month);
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    // returns statistics about the amount of reservations, that were made every hour of that day regardless the workout lesson
    @GetMapping("/getPopularHoursOfDay/{day}")
    public ResponseEntity<Map<String,Long>>  getMostReservedHourOfDay(@PathVariable String day){
        Map<String, Long> list = statisticsService.getPopularHoursOfDay(LocalDate.parse(day));
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @GetMapping("/startAllSchedules")
    public void startSchedule(){
        postProcessor.postProcessAfterInitialization(reservationsService,SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(subService,SCHEDULED_TASKS);
        postProcessor.postProcessAfterInitialization(statisticsService,SCHEDULED_TASKS);
        System.out.println("started scheduling");
    }

    @GetMapping(value = "/stopAllSchedules")
    public void stopSchedule() {
        postProcessor.postProcessBeforeDestruction(reservationsService, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(subService, SCHEDULED_TASKS);
        postProcessor.postProcessBeforeDestruction(statisticsService, SCHEDULED_TASKS);
        System.out.println("stopped scheduling");
    }
}
