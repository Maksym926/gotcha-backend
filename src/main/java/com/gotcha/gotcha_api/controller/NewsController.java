package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.dto.NewsResponse;
import com.gotcha.gotcha_api.service.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@Slf4j
public class NewsController {

    @Autowired
    NewService newService;

    @GetMapping("/news")
    public ResponseEntity<NewsResponse> getNewsPage(@PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable){
        log.info("Getting news page from DB: ");
      return newService.getAllNews(pageable);
    }


}
