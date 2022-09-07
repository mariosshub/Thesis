package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.SubscriptionWorkoutLessons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionWorkoutLessonsRepository extends JpaRepository<SubscriptionWorkoutLessons,Long> {
    List<SubscriptionWorkoutLessons> findAllByExpired(Boolean expired);

    long countSubscriptionWorkoutLessonsByWorkoutLessonIdAndExpired(Long id, Boolean expired);


    @Query("select sum(s.lessonCost) FROM SubscriptionWorkoutLessons s where s.expired = :expired and s.workoutLesson.id = :workoutId")
    Double sumSubscriptionWorkoutLessonsLessonCostByExpiredAndWorkoutLessonId (@Param("expired") Boolean expired, @Param("workoutId") Long id);

}
