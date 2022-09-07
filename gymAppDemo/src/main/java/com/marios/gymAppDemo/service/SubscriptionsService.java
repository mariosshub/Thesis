package com.marios.gymAppDemo.service;

import com.marios.gymAppDemo.request.AddSubRequest;
import com.marios.gymAppDemo.request.EditSubRequest;
import com.marios.gymAppDemo.customException.CustomNotFoundException;
import com.marios.gymAppDemo.model.*;
import com.marios.gymAppDemo.repository.CustomerRepository;
import com.marios.gymAppDemo.repository.SubscriptionsRepository;
import com.marios.gymAppDemo.repository.SubscriptionWorkoutLessonsRepository;
import com.marios.gymAppDemo.repository.WorkoutLessonsRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.marios.gymAppDemo.model.CostConstant.*;

@Service
@Transactional
@AllArgsConstructor
public class SubscriptionsService {
    private final SubscriptionsRepository subRepository;
    private final CustomerRepository customerRepository;
    private final WorkoutLessonsRepository workoutLessonsRepository;
    private final SubscriptionWorkoutLessonsRepository subscriptionWorkoutLessonsRepository;

    // return the Subscription of a customer by username
    public Subscription getSubByCustomersUsername(String username){
        return subRepository.findByCustomerUsername(username).orElseThrow(()->new CustomNotFoundException("sub_not_found"));
    }

    // return the Subscription of a customer by email
    public Subscription getSubByEmail(String email){
        return subRepository.findByCustomerEmail(email).orElseThrow(()->new CustomNotFoundException("sub_cust_not_found"));
    }

    // creates a subscription for a workout lesson and returns it
    public Subscription addSubToCustomer(AddSubRequest addSubRequest){
        Customer customer = customerRepository.findCustomerByEmail(addSubRequest.getCustomersEmail())
                .orElseThrow(() -> new CustomNotFoundException("cust_email_not_found"));
        WorkoutLessons workoutLesson = workoutLessonsRepository.findById(addSubRequest.getWorkoutId())
                .orElseThrow(()-> new CustomNotFoundException("workout_not_found"));

        Optional<Subscription> subscription = subRepository.findByCustomerId(customer.getId());

        String workoutCostType = addSubRequest.getCost();
        //the customer has a subscription to a workout lesson
        if(subscription.isPresent()){
            Subscription presentSub = subscription.get();
            double presentSubCost = presentSub.getCost();
            List<SubscriptionWorkoutLessons> subscriptionWorkoutLessonsList = presentSub.getSubscriptionWorkoutLessons();

            // check if customer has a subscription to the workout lesson requested
            for (SubscriptionWorkoutLessons subWorkoutLesson: subscriptionWorkoutLessonsList) {
                if(subWorkoutLesson.getWorkoutLesson().getId().equals(addSubRequest.getWorkoutId())){
                    throw new CustomNotFoundException("sub_exists");
                }
            }

            SubscriptionWorkoutLessons subscriptionWorkoutLessons = new SubscriptionWorkoutLessons();
            subscriptionWorkoutLessons.setCost(workoutCostType);
            subscriptionWorkoutLessons.setAmount(addSubRequest.getAmount());
            subscriptionWorkoutLessons.setWorkoutLesson(workoutLesson);
            subscriptionWorkoutLessons.setCreatedAt(LocalDate.now().toString());

            switch (workoutCostType){
                case COSTPERDAY:
                    presentSubCost += (workoutLesson.getCostPerDay() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerDay() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusDays(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERWEEK:
                    presentSubCost += (workoutLesson.getCostPerWeek() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerWeek() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusWeeks(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERMONTH:
                    presentSubCost += (workoutLesson.getCostPerMonth() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerMonth() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusMonths(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERYEAR:
                    presentSubCost += (workoutLesson.getCostPerYear() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerYear() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusYears(addSubRequest.getAmount()).toString());
                    break;
            }
            subscriptionWorkoutLessonsRepository.save(subscriptionWorkoutLessons);

            subscriptionWorkoutLessonsList.add(subscriptionWorkoutLessons);
            presentSub.setSubscriptionWorkoutLessons(subscriptionWorkoutLessonsList);
            presentSub.setCost(presentSubCost);

            return subRepository.save(presentSub);
        }
        // its the first time that the customer subscribes to a workout lesson
        else{
            Subscription sub = new Subscription();
            sub.setCustomer(customer);

            SubscriptionWorkoutLessons subscriptionWorkoutLessons = new SubscriptionWorkoutLessons();
            subscriptionWorkoutLessons.setCreatedAt(LocalDate.now().toString());
            subscriptionWorkoutLessons.setCost(workoutCostType);
            subscriptionWorkoutLessons.setAmount(addSubRequest.getAmount());
            subscriptionWorkoutLessons.setWorkoutLesson(workoutLesson);

            switch (workoutCostType){
                case COSTPERDAY:
                    sub.setCost(workoutLesson.getCostPerDay() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerDay() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusDays(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERWEEK:
                    sub.setCost(workoutLesson.getCostPerWeek() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerWeek() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusWeeks(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERMONTH:
                    sub.setCost(workoutLesson.getCostPerMonth() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerMonth() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusMonths(addSubRequest.getAmount()).toString());
                    break;
                case COSTPERYEAR:
                    sub.setCost(workoutLesson.getCostPerYear() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setLessonCost(workoutLesson.getCostPerYear() * addSubRequest.getAmount());
                    subscriptionWorkoutLessons.setExpiresAt(LocalDate.now().plusYears(addSubRequest.getAmount()).toString());
                    break;
            }

            subscriptionWorkoutLessons.setExpired(false);
            subscriptionWorkoutLessonsRepository.save(subscriptionWorkoutLessons);

            List<SubscriptionWorkoutLessons> subWorkoutLessonsList = new ArrayList<>();
            subWorkoutLessonsList.add(subscriptionWorkoutLessons);
            sub.setSubscriptionWorkoutLessons(subWorkoutLessonsList);
            return subRepository.save(sub);
        }
    }

    // updates the expired subscription of a workout lesson and returns the Subscription
    public Subscription updateSub(EditSubRequest editSubRequest){
        Subscription updateSubscription = subRepository.findById(editSubRequest.getSubId()).orElseThrow(() -> new CustomNotFoundException("sub_not_found"));
        SubscriptionWorkoutLessons updateSubscriptionWorkoutLesson = subscriptionWorkoutLessonsRepository.
                findById(editSubRequest.getSubWorkoutLessonId()).orElseThrow(() -> new CustomNotFoundException("lesson_sub_not_found"));

        if(updateSubscriptionWorkoutLesson.isExpired()){
            double totalCost = updateSubscription.getCost();
            // remove the old cost from the total cost
            totalCost -= updateSubscriptionWorkoutLesson.getLessonCost();

            WorkoutLessons workoutLesson = updateSubscriptionWorkoutLesson.getWorkoutLesson();
            switch (editSubRequest.getCost()){
                case COSTPERDAY:
                    totalCost += workoutLesson.getCostPerDay() * editSubRequest.getAmount();
                    updateSubscriptionWorkoutLesson.setLessonCost(workoutLesson.getCostPerDay() * editSubRequest.getAmount());
                    updateSubscriptionWorkoutLesson.setExpiresAt(LocalDate.now().plusDays(editSubRequest.getAmount()).toString());
                    break;
                case COSTPERWEEK:
                    totalCost += workoutLesson.getCostPerWeek() * editSubRequest.getAmount();
                    updateSubscriptionWorkoutLesson.setLessonCost(workoutLesson.getCostPerWeek() * editSubRequest.getAmount());
                    updateSubscriptionWorkoutLesson.setExpiresAt(LocalDate.now().plusWeeks(editSubRequest.getAmount()).toString());
                    break;
                case COSTPERMONTH:
                    totalCost += workoutLesson.getCostPerMonth() * editSubRequest.getAmount();
                    updateSubscriptionWorkoutLesson.setLessonCost(workoutLesson.getCostPerMonth() * editSubRequest.getAmount());
                    updateSubscriptionWorkoutLesson.setExpiresAt(LocalDate.now().plusMonths(editSubRequest.getAmount()).toString());
                    break;
                case COSTPERYEAR:
                    totalCost += workoutLesson.getCostPerYear() * editSubRequest.getAmount();
                    updateSubscriptionWorkoutLesson.setLessonCost(workoutLesson.getCostPerYear() * editSubRequest.getAmount());
                    updateSubscriptionWorkoutLesson.setExpiresAt(LocalDate.now().plusYears(editSubRequest.getAmount()).toString());
                    break;
            }
            // update the total cost with the new cost
            updateSubscription.setCost(totalCost);

            updateSubscriptionWorkoutLesson.setCreatedAt(LocalDate.now().toString());
            updateSubscriptionWorkoutLesson.setCost(editSubRequest.getCost());
            updateSubscriptionWorkoutLesson.setAmount(editSubRequest.getAmount());
            updateSubscriptionWorkoutLesson.setExpired(false);
            subscriptionWorkoutLessonsRepository.save(updateSubscriptionWorkoutLesson);

            return subRepository.save(updateSubscription);

        }
        else{
            throw new CustomNotFoundException("sub_not_expired");
        }
    }

    // checks if customer's subscriptions are expired
    @Scheduled(cron = "0 0 0 * * *") // at 00:00 every day
    public void checkSubExpirationScheduler(){
        List<SubscriptionWorkoutLessons> subscriptionWorkoutLessons = subscriptionWorkoutLessonsRepository.findAllByExpired(false);
        for (SubscriptionWorkoutLessons workoutLesson: subscriptionWorkoutLessons) {
            LocalDate lessonExpires;
            lessonExpires = LocalDate.parse(workoutLesson.getExpiresAt());

            // check the current time and compare it with the time that the subscription expires
            if(lessonExpires != null && LocalDate.now().isAfter(lessonExpires)){
                workoutLesson.setExpired(true);
                subscriptionWorkoutLessonsRepository.save(workoutLesson);
            }
        }
    }

    // find customer's subscription by username and return true if it is expired, or false otherwise
    public boolean checkCustomersSubExpiration(String username){
        boolean hasExpiredLessons = false;
        Subscription subscription = subRepository.findByCustomerUsername(username).orElseThrow(() -> new CustomNotFoundException("sub_not_found"));

        for (SubscriptionWorkoutLessons lesson: subscription.getSubscriptionWorkoutLessons()) {
            if(lesson.isExpired()){
                hasExpiredLessons = true;
            }
            else{
                LocalDate lessonExpires;
                try {
                    lessonExpires = LocalDate.parse(lesson.getExpiresAt());
                }
                catch (DateTimeException e){
                    throw new CustomNotFoundException("smth_wrong_message");
                }
                if(lessonExpires != null && LocalDate.now().isAfter(lessonExpires)){
                    hasExpiredLessons = true;
                    lesson.setExpired(true);
                    subscriptionWorkoutLessonsRepository.save(lesson);
                }
            }
        }
        return hasExpiredLessons;
    }
}
