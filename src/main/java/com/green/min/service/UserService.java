package com.green.min.service;

import com.green.min.dto.UserResponse;
import com.green.min.entity.User;
import com.green.min.exceptions.AuthenticationFailureException;
import com.green.min.exceptions.ResourceNotFoundException;
import com.green.min.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

public class UserService {

    public final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // 로그인 확인
    public User getLoginUser(HttpSession session) {
        Object userIdAsString = session.getAttribute(("userId"));
        if (userIdAsString == null) {  // 스프링부트 내부 로직때문에 userId null이면 id를 못찾은것!
            throw new AuthenticationFailureException("로그인이 필요합니다.");
        }

        int userId = Integer.parseInt(userIdAsString.toString()); // 세션 장부에서 지금 로그인 한 사용자 id 추출

        Optional<User> optionalUser = UserRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다");
        }

        return optionalUser.get();
    }



    // 유저 정보 조회 (내가 내 정보 조회)
    public UserResponse getDetailUser(HttpSession session) {
        User user = getLoginUser(session);  // 로그인 한 user.

        Optional<User> userOptional = UserRepository.findbyId();

        UserResponse response = new UserResponse(

                // DTO로 변환
        )


        user.getName()  // 왜썼는지 잘 모르겠음


        return response;
        
    }

    // 유저 정보 수정

    // 로그인 한 유저를 가져온다 (세션)
    // DB에 가서 user테이블의 id가 로그인 한 유저의 id와 같은지 확인한다.
    // 유저의 요청 내용 -> username, name이 각각 null인지 확인.
    // null이 아니라면 내용이 변경(수정)된 것이므로
    // UserRepository에게 db user테이블의 not null?? 부분을 update 해달라고 한다.


    // 유저 삭제

    // 로그인 한 유저를 가져온다 (세션)
    // UserRepository에게 db user테이블의 is_deleted를 true로 update 해달라고 한다.


}
