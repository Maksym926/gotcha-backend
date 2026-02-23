package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.EventRSVP;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRSVPRepo extends JpaRepository<EventRSVP, Long> {
    List<EventRSVP> findByUser_UserId(Long userId);
    List<EventRSVP> findByEvent_EventId(Long eventId);


}
