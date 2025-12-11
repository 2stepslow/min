package com.green.min.service;

import com.green.min.dto.UserLoginRequest;
import com.green.min.dto.UserRegisterRequest;
import com.green.min.entity.User;
import com.green.min.exceptions.AuthenticationFailureException;
import com.green.min.exceptions.AuthorizationFailureException;
import com.green.min.exceptions.ResourceNotFoundException;
import com.green.min.exceptions.ValidationException;
import com.green.min.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public AuthService (UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 로그인 체크
    public User getLoginUser(HttpSession session) {
        Object userIdAsString = session.getAttribute(("userId"));
        if (userIdAsString == null) {  // 스프링부트 내부 로직때문에 userId null이면 id를 못찾은것!
            throw new AuthenticationFailureException("로그인이 필요합니다.");
        }

        int userId = Integer.parseInt(userIdAsString.toString()); // 세션 장부에서 지금 로그인 한 사용자 id 추출

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다");
        }

        return optionalUser.get();
    }


    // 로그인 처리
    public void login(UserLoginRequest userLoginRequest, HttpSession session){
        User user = userRepository.findByUsername(userLoginRequest.getUsername());

        List<UserLoginRequest> userLoginRequests = new ArrayList<>();

        if(user == null) {
            throw new ResourceNotFoundException("회원존재하지않음");
    }
        if(!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            // 실패했으면 401
            throw new AuthorizationFailureException("비밀번호틀림");
        }

        session.setAttribute("userId", user.getId());  // AuthController에 적어두면 다른 controller에서도 찾아볼수 있음.(장부 카톡공유)
        session.setAttribute("username", user.getUsername());
    }


    // 회원가입 처리
    public void register(UserRegisterRequest userRegisterRequest) {
        // 검증 로직 추가 1.유저네임, 2.비밀번호, 3.닉네임
        if(userRegisterRequest.getUsername() == null) {
            throw new ValidationException("유저 이름은 필수입니다.");
        }
        if(userRegisterRequest.getPassword() == null ){
            throw new ValidationException("비밀번호는 필수입니다.");
        }
        if(userRegisterRequest.getName() == null) {
            throw new ValidationException("닉네임 입력은 필수입니다.");
        }

        if(userRegisterRequest.getPassword().length() < 6) {
            throw new ValidationException("비밀번호는 6자 이상이어야 합니다");
        }

        String encodedPassword = passwordEncoder.encode(userRegisterRequest.getPassword());

        User user = new User();
//                userRegisterRequest.getUsername(),
//                encodedPassword,
//                userRegisterRequest.getName(),
//                LocalDateTime.now()

                user.setUsername(userRegisterRequest.getUsername());
                user.setPassword(encodedPassword);
                user.setName(userRegisterRequest.getName());

        userRepository.save(user);
        // 비밀번호는 평문저장하면 위험하므로 넣기 전에 ‘해싱’ 함
        // 프론트 개발자와 협의 해야 함! 프론트가 id, passwd JSON으로 넘길거다
    }


    // 로그아웃 처리
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
