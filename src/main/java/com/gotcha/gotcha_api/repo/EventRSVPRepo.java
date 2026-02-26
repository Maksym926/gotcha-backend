package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.EventRSVP;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRSVPRepo extends JpaRepository<EventRSVP, Long> {
    Optional<List<EventRSVP>> findByUser_UserId(Long userId);
    Optional<List<EventRSVP>> findByEvent_EventId(Long eventId);


}
