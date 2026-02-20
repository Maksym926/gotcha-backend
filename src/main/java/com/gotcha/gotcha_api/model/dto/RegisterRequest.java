package com.gotcha.gotcha_api.model.dto;

import com.gotcha.gotcha_api.enums.Role;

public record RegisterRequest(
        String userName,
        String email,
        String password

) {


}
