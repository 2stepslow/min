package com.green.min.repository;

import com.green.min.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {  // (JpaRepository: 기본세팅 되어있는 완성된 코드)
    // SELECT * FROM boards WHERE author = ?
    List<Board> findBoardsByAuthor(int author);  // 보드에서 author정보를 찾아서 괄호안에 넣겠다

    // SELECT * FROM boards WHERE is_deleted = 0
    List<Board> findBoardsByIsDeletedFalse();
}
