package com.gotcha.gotcha_api.model;

import com.gotcha.gotcha_api.model.dto.EventResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {

    private List<EventResponse> events;


}
