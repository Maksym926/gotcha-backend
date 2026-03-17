package com.gotcha.gotcha_api.repo;

import com.gotcha.gotcha_api.model.StaticContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaticContentRepo extends JpaRepository<StaticContent, Long> {
    Optional<StaticContent> findBySectionKey(String sectionKey);
}
