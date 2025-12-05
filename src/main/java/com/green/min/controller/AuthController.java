package com.green.min.controller;

import com.green.min.PasswordConfig;
import com.green.min.dto.UserLoginRequest;
import com.green.min.dto.UserRegisterRequest;
import com.green.min.entity.User;
import com.green.min.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {  // 스프링이 new된 userRepository를 자동으로 넣어줄것. 책임이 역전되었다=IoC
        // 스프링이 알아서 new 해준 다음에 생성자로 넣어줌. 우린 그냥 갖다 쓰면 됨.
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    // a클래스를 b에서 쓰고싶으면 new 사용해 객체 만들수 있음. static이면 갖다쓸수 있지만 static 아니면..

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody UserLoginRequest userLoginRequest,
            HttpSession session
    ) {
        // username: green

        // username, password
        // SELECT * FROM users WHERE username={사용자기준 username}
        // 1. 사용자로부터 ID/PW 받음
        // 2. JPA 이용해 DB에 조회 해 있는사람인지 확인
        User user = userRepository.findByUsername(userLoginRequest.getUsername());
        if(user == null) {
            return ResponseEntity.notFound().build();
        }

        // 3. 그 유저의 PW가 사용자가 입력한 PW와 일치하는지 확인
        // encode 할때마다 다른 솔트값을 쓰기 때문에, 분명 똑같은 비밀번호 쳤는데도 equals가 계속 false가 뜸.. 어떡함?
        // 이때 사용하는게 passwordEncoder.matches(사용자가 입력한 평문값, DB에 있는 해싱된 값)

        if(!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            // 실패했으면 401
            return ResponseEntity.status(401).build();
        }


        // 4. 로그인 되면, 세션이란걸 발급해 응답 해 준다
        // 4-1. 세션에 사용자 정보 저장 (장부 작성)
        // 장부만 작성하고 세션 ID를 응답으로 내려주는 코드는 없음! 근데 내려간다
        // -> 스프링부트가 "너 세션 쓰네? 그럼 응답으로 세션 ID 내려야겠네?" 하고 자동으로 내려주기 때문
        session.setAttribute("userId", user.getId());  // AuthController에 적어두면 다른 controller에서도 찾아볼수 있음.(장부 카톡공유)
        session.setAttribute("username", user.getUsername());
        // 위 코드를 실행하는 순간 서버의 세션(장부)에 아래와 같은 내용이 들어감
        //      ID        | userID | username |  exp
        //  A908DA(랜덤값) |    3   |   정로션  | 30분후
        // 그리고 스프링부트가 ID(A908DA)를 알아서 응답으로 내림
        // 그럼 브라우저가 그 sessionID를 다음부터 다른 요청에 붙여서 올려줌




        // 4-2. 입장권 발급
        // -> 스프링부트가 자동으로 해줌
        // 1. 세션에 사용자 정보를 저장하는 과정(장부 작성)에서 자동으로 Session ID가 배정됨
        // 2. 스프링부트가 알아서 Session ID를 쿠키로 내림
        return ResponseEntity.ok().build();

//        // 4-2. 유저에게 돌려줄 세션 정보 생성 (입장권 생성)
//        Map<String, Object> response = new HashMap<>();
//        response.put("userId", user.getId());
//        response.put("username", user.getUsername());
//
//        return ResponseEntity.ok(response);
//        // 리스폰스 엔티티 안에 해시맵 넣어서 리턴
    }


    @PostMapping("/register")  // 회원가입은 DB에 없던걸 생성
    public void register(@RequestBody UserRegisterRequest userRegisterRequest) {
        // DB에 집어넣는거 -> Repositor

        //  [회원가입]
        //  1. 사용자가 입력한 정보를 DB에 넣음. 끝!
        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = new User(
                userRegisterRequest.getUsername(),
                encodedPassword,
                userRegisterRequest.getName()
        );

        userRepository.save(user);
        //  2. 근데 비밀번호는 평문저장하면 위험하므로 넣기 전에 ‘해싱’ 함
        // 프론트 개발자와 협의 해야 함! 프론트가 id, passwd JSON으로 넘길거다
    }

//  [로그아웃]
//  1. 서버에서 발급한 세션 지움
//  2. 브라우저에게 ‘난 지웠으니 너도 알아서 지워’ 말해줌 → 자바는 요청 안받으면 끝인데 사용자에게는 로그인 되어있는 화면으로 보여질수 있기 때문에 브라우저에게도 시켜야 함
//
//  [게시글 조회 등 일반]
//  1. 행동 하기 전, 사용자가 넘긴 세션이 내가(서버가) 발급한게 맞는지 확인
//  2. 세션이 없거나 만료되었거나 등등이면 해주지말고 거절

}
