package com.company.gojob.account_service.repository;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.response.UserCredentialResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    @Query(nativeQuery = true, value = "SELECT * FROM user_credential")
    List<UserCredential> findAllUserCredentials();

    @Query(nativeQuery = true, value = "SELECT * FROM user_credential AS u WHERE u.id = :id LIMIT 1 ")
    UserCredential findUserCredentialById(@Param("id") String id);


    @Query(value = "SELECT u.id AS userCredentialId, u.username AS username, u.email AS email " +
                    "FROM user_credential AS u " +
                    "WHERE u.username = :username ", nativeQuery = true)
    UserCredentialDTO getUserCredentialByUserName(String username);


    UserCredential findUserCredentialByUsername(String username);
}
