package com.green.min.controller;

import com.green.min.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 내 정보 조회
    public ResponseEntity<?> getLoginUser(HttpSession session) {
        try {

            return
        } catch () {
            return
        }
    }

    // 내 정보 수정


    // 탈퇴(내가)
}
