package com.example.rtcharityremainderbot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT u.privateCode FROM Users u WHERE u.chatId = :id")
    String findPrivateCodeById(@Param("id") Long id);

    @Query("SELECT u.isAdmin FROM Users u WHERE u.chatId = :id")
    Boolean checkIsAdminById(@Param("id") Long id);

    @Query("SELECT u.userName FROM Users u WHERE u.privateCode = :code")
    User findUserByCode(@Param("code") String code);

    @Query("SELECT u FROM Users u WHERE u.registeredTime != null ")
    List<User> findRegisteredUsers();

    @Query("SELECT u FROM Users u WHERE u.registeredTime is null ")
    List<User> findNotRegisteredUsers();

    @Query("SELECT u.userName FROM Users u WHERE u.registeredTime != null ")
    List<String> findRegisteredUserNames();

    @Query("SELECT u.chatId FROM Users u WHERE u.userName = 'assemgulw' or u.userName = 'aikerim_abdulla'")
    List<Long> findAdmins();

    @Query("SELECT u.privateCode FROM Users u")
    List<String> findAllPrivateCodes();

    @Query("SELECT count(*) FROM Users u WHERE u.registeredTime is not null")
    Integer findRegisteredCount();

    @Query("SELECT count(*) FROM Users u")
    Integer findAllUsersCount();

}
