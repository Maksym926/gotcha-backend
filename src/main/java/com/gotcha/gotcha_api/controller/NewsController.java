package com.gotcha.gotcha_api.controller;

import com.gotcha.gotcha_api.model.News;
import com.gotcha.gotcha_api.service.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
@Slf4j
public class NewsController {

    @Autowired
    NewService newService;

    @GetMapping("/news")
    public ResponseEntity<News> getNewsPage(){
        log.info("Getting news page from DB: ");
      return newService.getAllNews();
    }


}
