package com.green.min.controller;

import com.green.min.entity.Test;
import com.green.min.repository.TestRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController  // 이 클래스는 사용자의 요청만 처리하는 controller(안내 직원) 역할
@RequestMapping("/api/test")  // url경로 미리 설정. URL /api/test 하위로 오는 요청만 처리
public class TestController {

//    @GetMapping("/hello")
//    // HTTP Method는 GET을 쓰겠다. 내 API가 GET이될지 POST가 될지 판단해서 지정 해 주고 URL도 깔끔하게 만들기.
//    // URL이 /hello로 끝나는 경우 아래 메서드를 실행하겠다
//    // -> /api/test/hello
//
//    public String test() {   // 각각 요청 받아서 배정받은애 들어올때마다 메서드를 실행해서 코드 실행시킴.
//        return "hello spring!";
//    }

    private final TestRepository testRepository;

    public TestController(TestRepository testRepository) {
        this.testRepository = testRepository;
    }

    @GetMapping("/test")
    public List<Test> getTest() {
        List<Test> results = testRepository.findAll();
        return results;
    }

}
