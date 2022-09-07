package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionsRepository extends JpaRepository<Subscription,Long> {
    Optional<Subscription> findByCustomerId (Long id);
    Optional<Subscription> findByCustomerUsername (String username);
    Optional<Subscription> findByCustomerEmail (String email);
}
