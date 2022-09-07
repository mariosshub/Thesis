package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.request.LessonAvailabilityRequest;
import com.marios.gymAppDemo.request.WorkoutLessonRequest;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.LessonAvailability;
import com.marios.gymAppDemo.model.WorkoutLessons;
import com.marios.gymAppDemo.repository.LessonAvailabilityRepository;
import com.marios.gymAppDemo.repository.WorkoutLessonsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class WorkoutLessonsService {
    private final WorkoutLessonsRepository workoutLessonsRepository;
    private final LessonAvailabilityRepository lessonAvailabilityRepository;

    // adds a new workout lesson and returns the object
    public WorkoutLessons addWorkoutLesson(WorkoutLessonRequest workoutLessons){
        WorkoutLessons newWorkoutLesson =  new WorkoutLessons();
        newWorkoutLesson.setName(workoutLessons.getName());
        newWorkoutLesson.setCostPerDay(workoutLessons.getCostPerDay());
        newWorkoutLesson.setCostPerWeek(workoutLessons.getCostPerWeek());
        newWorkoutLesson.setCostPerMonth(workoutLessons.getCostPerMonth());
        newWorkoutLesson.setCostPerYear(workoutLessons.getCostPerYear());
        newWorkoutLesson.setDescription(workoutLessons.getDescription());
        newWorkoutLesson.setMaxPeeps(workoutLessons.getMaxPeeps());
        return workoutLessonsRepository.save(newWorkoutLesson);
    }

    // returns a list of all the workout lessons
    public List<WorkoutLessons> findAllWorkoutLessons(){return workoutLessonsRepository.findAll();}

    // updates the fields of a workout lesson and returns the object
    public WorkoutLessons updateWorkoutLesson(WorkoutLessonRequest workoutLesson){
        WorkoutLessons newLesson = workoutLessonsRepository.findById(workoutLesson.getId()).orElseThrow(() ->new CustomNotFoundException("lesson_not_found"));
        newLesson.setName(workoutLesson.getName());
        newLesson.setDescription(workoutLesson.getDescription());
        newLesson.setCostPerDay(workoutLesson.getCostPerDay());
        newLesson.setCostPerWeek(workoutLesson.getCostPerWeek());
        newLesson.setCostPerMonth(workoutLesson.getCostPerMonth());
        newLesson.setCostPerYear(workoutLesson.getCostPerYear());
        newLesson.setMaxPeeps(workoutLesson.getMaxPeeps());
        return workoutLessonsRepository.save(newLesson);
    }

    // adds an availability to the availability list of a workout lesson
    public WorkoutLessons addLessonAvailability(LessonAvailabilityRequest lessonAvailabilityRequest){
        WorkoutLessons workoutWithNewAvailability = lessonAvailabilityRequest.getWorkoutLessons();
        List<LessonAvailability> lessonAvailabilities = new ArrayList<>();

        if(workoutWithNewAvailability.getLessonAvailabilities() != null){
           lessonAvailabilities = workoutWithNewAvailability.getLessonAvailabilities();
        }

        LessonAvailability lessonAvailability = new LessonAvailability();
        lessonAvailability.setDate(lessonAvailabilityRequest.getDate());
        lessonAvailability.setStartingHour(lessonAvailabilityRequest.getStartingHour());
        lessonAvailability.setEndHour(lessonAvailabilityRequest.getEndHour());
        lessonAvailability.setWorkoutLessons(workoutWithNewAvailability);
        lessonAvailabilities.add(lessonAvailability);

        workoutWithNewAvailability.setLessonAvailabilities(lessonAvailabilities);

        lessonAvailabilityRepository.save(lessonAvailability);
        return workoutLessonsRepository.save(workoutWithNewAvailability);
    }

    // checks if a workout lesson has no availability
    public boolean checkWorkoutLessonsAvailabilitiesIfEmpty(){
        return workoutLessonsRepository.existsByLessonAvailabilities_Empty();
    }

}
