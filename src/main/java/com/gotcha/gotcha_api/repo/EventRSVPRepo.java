package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.EventRSVP;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRSVPRepo extends JpaRepository<EventRSVP, Long> {
    Optional<Page<EventRSVP>> findByUser_UserId(Long userId, Pageable pageable);
    Optional<Page<EventRSVP>> findByEvent_EventId(Long eventId, Pageable pageable);
    Optional<EventRSVP> findByUser_UserIdAndEvent_EventId(Long userId, Long eventId);
    void deleteByUser_UserId(Long userId);

}
