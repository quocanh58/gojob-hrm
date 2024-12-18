package com.company.gojob.account_service.repository;

import com.company.gojob.account_service.dto.UserCredentialDTO;
import com.company.gojob.account_service.model.UserCredential;
import com.company.gojob.account_service.payload.response.UserCredentialResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Transactional
    @Modifying
    @Query(value = "UPDATE user_credential AS a SET a.email = :email, a.username = :username WHERE a.id = :id", nativeQuery = true)
    int updateUserCredentialById(@Param("id") String id, @Param("email") String email, @Param("username") String username);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM user_credential WHERE id = :id", nativeQuery = true)
    int deleteUserCredentialById(@Param("id") String id);

    UserCredential findUserCredentialByUsername(String username);


}
