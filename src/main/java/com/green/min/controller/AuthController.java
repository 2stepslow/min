package com.green.min.controller;

import com.green.min.dto.UserLoginRequest;
import com.green.min.dto.UserRegisterRequest;
import com.green.min.exceptions.AuthorizationFailureException;
import com.green.min.exceptions.ResourceNotFoundException;
import com.green.min.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1. 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody UserLoginRequest userLoginRequest,
            HttpSession session
    ) {

        try {
            authService.login(userLoginRequest, session);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AuthorizationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 2. 회원가입 API
    @PostMapping("/register")  // 회원가입은 DB에 없던걸 생성
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        authService.register(userRegisterRequest);
        return ResponseEntity.ok().build();

    }


//  [로그아웃]
//  1. 서버에서 발급한 세션 지움
//  2. 브라우저에게 ‘난 지웠으니 너도 알아서 지워’ 말해줌 → 자바는 요청 안받으면 끝인데 사용자에게는 로그인 되어있는 화면으로 보여질수 있기 때문에 브라우저에게도 시켜야 함
//
//  [게시글 조회 등 일반]
//  1. 행동 하기 전, 사용자가 넘긴 세션이 내가(서버가) 발급한게 맞는지 확인
//  2. 세션이 없거나 만료되었거나 등등이면 해주지말고 거절

    // 직원 단톡방에 이 번호 가진 사람 내쫓을거니 놀이기구 타려하면 거절하라.
    // 장부에서 삭제하는건 백앤드가 함.
    // 쫓아내는 건 프론트앤드가 함. -> 쿠키에 들어있는 jsession 지워야 함
    // 티켓id 저장은 브라우저에 되어 있음.
    // 로그아웃 요청 들어오면, 특별한 요청 내려야 함.
    // 우리는 서버에서 없애고, ResponseEntity에 ~~.ok() 내려야 함


    // 3. 로그아웃 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok().build();
    }

}
