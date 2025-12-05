package com.green.min.repository;

import com.green.min.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {  // (JpaRepository: 기본세팅 되어있는 완성된 코드)

}
