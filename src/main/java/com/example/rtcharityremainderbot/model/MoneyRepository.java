package com.example.rtcharityremainderbot.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoneyRepository extends CrudRepository<Money, Long> {
    @Query("SELECT m FROM Money m ORDER BY m.registeredTime DESC")
    List<Money> findAllOrderByRegisteredTimeDesc();

    default Double getLastAmount() {
        List<Money> moneyList = findAllOrderByRegisteredTimeDesc();
        if (!moneyList.isEmpty()) {
            return moneyList.get(0).getAmount();
        }
        return null;
    }
}
