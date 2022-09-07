package com.marios.gymAppDemo.repository;

import com.marios.gymAppDemo.model.LoginDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LoginDetailsRepository extends JpaRepository<LoginDetails ,Long> {
    Optional<LoginDetails> findByUsername(String username);
}
