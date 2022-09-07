package com.marios.gymAppDemo.controller;

import com.marios.gymAppDemo.request.CountForReservationsRequest;
import com.marios.gymAppDemo.request.MakeReservationRequest;
import com.marios.gymAppDemo.request.UpdateProfileRequest;
import com.marios.gymAppDemo.response.AvailabilityForResResponse;
import com.marios.gymAppDemo.response.ReservationsResponse;
import com.marios.gymAppDemo.model.*;
import com.marios.gymAppDemo.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/customer")
@AllArgsConstructor
public class CustomerController {
    private final CustomerService service;
    private final SubscriptionsService subService;
    private final LessonAvailabilityService lessonAvailabilityService;
    private final ReservationsService reservationsService;

    // shows profile details
    @GetMapping("/showProfile")
    public ResponseEntity<Customer> getCustomerById(Principal principal){
        Customer customer =  service.findCustomerByUsername(principal.getName());
        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    // updates profile details
    @PutMapping("/updateProfile")
    public ResponseEntity<Customer> updateCustomer(@RequestBody UpdateProfileRequest profileRequest, Principal principal){
        Customer updateCustomer = service.updateCustomer(profileRequest, principal.getName());
        return new ResponseEntity<>(updateCustomer,HttpStatus.CREATED);
    }

    // updates photo url
    @PutMapping("/updatePhoto/{imgUrl}")
    public ResponseEntity<Boolean> updatePhoto(@PathVariable String imgUrl, Principal principal){
        Boolean flag = service.updatePhoto(imgUrl, principal.getName());
        return new ResponseEntity<>(flag,HttpStatus.CREATED);
    }

    // finds the availabilities of all workout lessons
    @GetMapping("/findAllAvailabilities")
    public ResponseEntity<List<AvailabilityForResResponse>> findAllAvailabilities(){
        return new ResponseEntity<>(lessonAvailabilityService.findAllAvailabilities(),HttpStatus.OK);
    }

    // creates a reservation
    @PostMapping ("/makeReservation")
    ResponseEntity<Boolean> makeReservation(@RequestBody MakeReservationRequest reservationRequest){
        boolean madeReservation = reservationsService.makeReservation(reservationRequest);
        return new ResponseEntity<>(madeReservation, HttpStatus.CREATED);
    }

    // finds all the reservations pending, cancelled, or made, of a customer
    @GetMapping("/findAllReservations")
    ResponseEntity<List<ReservationsResponse>> findAllReservations(Principal principal){
        List<ReservationsResponse> reservations = reservationsService.findAllReservationsByUsernameAndOrderByDate(principal.getName());
        return new ResponseEntity<>(reservations,HttpStatus.OK);
    }

    // cancels a reservation
    @PutMapping("/cancelReservation/{id}")
    ResponseEntity<Boolean> cancelReservation(@PathVariable Long id){
        boolean reservation = reservationsService.cancelReservation(id);
        return new ResponseEntity<>(reservation,HttpStatus.CREATED);
    }

    // returns the number of reservations of a workout lesson, at a specific date and time
    @PostMapping("/countReservations")
    ResponseEntity<Long> countReservations (@RequestBody CountForReservationsRequest countReq){
        long count = reservationsService.countReservations(countReq);
        return new ResponseEntity<>(count,HttpStatus.OK);
    }

    // finds the customers subscription
    @GetMapping("/findSub")
    public ResponseEntity<Subscription> getSub(Principal principal){
        Subscription subscription = subService.getSubByCustomersUsername(principal.getName());
        return new ResponseEntity<>(subscription,HttpStatus.OK);
    }

    // checks if the subscription of a workout lesson is expired
    @PutMapping("/checkSubExpiration")
    public ResponseEntity<Boolean> checkSubExpiration(Principal principal){
        Boolean response = subService.checkCustomersSubExpiration(principal.getName());
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

}
