package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.response.AvailabilityForResResponse;
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
public class LessonAvailabilityService {
    private final LessonAvailabilityRepository lessonAvailabilityRepository;
    private final WorkoutLessonsRepository workoutLessonsRepository;

    //returns a list of all workout lessons availabilities
    public List<AvailabilityForResResponse> findAllAvailabilities(){
        List<LessonAvailability> lessonAvailabilities = lessonAvailabilityRepository.findAll();

        List<AvailabilityForResResponse> availabilityForResResponses = new ArrayList<>();
        for (LessonAvailability availability: lessonAvailabilities) {
            availabilityForResResponses.add(new AvailabilityForResResponse(availability.getId(),availability.getWorkoutLessons().getId(), availability.getWorkoutLessons().getName(),
                    availability.getDate(), availability.getStartingHour(), availability.getEndHour(),availability.getWorkoutLessons().getMaxPeeps()));
        }

        return availabilityForResResponses;
    }

    //deletes the workout lesson's availability by id and returns the workout lesson
    public WorkoutLessons deleteLessonAvailability(Long id){
        LessonAvailability deleteLessonAvailability = lessonAvailabilityRepository.findById(id).orElseThrow(() -> new CustomNotFoundException("availability_not_found"));
        WorkoutLessons workoutLesson = deleteLessonAvailability.getWorkoutLessons();

        List<LessonAvailability> lessonAvailabilities = workoutLesson.getLessonAvailabilities();
        lessonAvailabilities.remove(deleteLessonAvailability);
        workoutLesson.setLessonAvailabilities(lessonAvailabilities);

        WorkoutLessons lesson = workoutLessonsRepository.save(workoutLesson);
        lessonAvailabilityRepository.deleteById(id);
        return lesson;
    }

}
