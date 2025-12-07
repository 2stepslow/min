package com.green.min.repository;

import com.green.min.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {  //<entity, Integer>
    // JPA가 함수 이름을 분석함
    // find - 찾는다 (SELECT)
    // by username - username으로 찾는다 (WHERE username = ?)
    // findByUsername - WHERE에 username 걸어서 SELECT 하고 싶구나!
    User findByUsername(String username);  // 파라미터 String username은 ?에 들어갈 값
    // = user 테이블에서 username 컬럼이 파라미터로 받은 값과 같은 row 한 줄을 찾아서 User 엔티티로 돌려 줘.
}
