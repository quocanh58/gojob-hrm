package com.company.gojob.account_service.repository;

import com.company.gojob.account_service.model.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<UserCredential, UUID> {

    Optional<UserCredential> findByUsername(String username);
}
