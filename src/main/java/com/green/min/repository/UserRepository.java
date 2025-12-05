package com.green.min.repository;

import com.green.min.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {  //<entity, Integer>
    // JPA가 함수 이름을 분석함
    // find - 찾는다
    // by username - username으로 찾는다
    // findByUsername - WHERE에 username 걸어서 SELECT 하고 싶구나!
    User findByUsername(String username);
}
