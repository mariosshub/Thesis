package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    void deleteCustomerById(Long id);
    Optional<Customer> findCustomerById(Long id);
    Optional<Customer> findCustomerByEmail(String email);
    Optional<Customer> findCustomerByUsername(String username);
}
