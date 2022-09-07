package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.WorkoutLessons;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutLessonsRepository extends JpaRepository<WorkoutLessons,Long> {
    Boolean existsByLessonAvailabilities_Empty();
}
