package com.green.min.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// Entity: JPA가 DB에서 가져온 데이터를 최초로 넣는 곳
@Entity
// JPA가 어느 테이블로 찾아가야 하는지 알려주는 곳
@Table(name = "boards")  // <- MySQL 테이블 이름. 이 클래스와 mysql의 boards 테이블 매칭
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Board {
    @Id    // DB에서 id라는 필드는 Primary Key라는 것을 알림
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // DB에서 id라는 필드는 auto_increment되는 값이니 건드리지 말라는 것
    private int id;

    @Column(name = "title", nullable = false)   // DB의 title 컬럼 정보와 이 title 필드와 1:1 매칭(값 여기 세팅)
    private String title;

    @Column(name = "content", nullable = false)   // DB의 content 컬럼 정보와 이 content 필드와 1:1 매칭(값 여기 세팅)
    private String content;

    @Column(name = "hits", nullable = false)
    private int hits;

    @Column(name= "author", nullable = false)
    private int author;

    @CreatedDate
    @Column(name = "created_datetime")
    private LocalDateTime createdDatetime;   // sql데이트타임은 java의 LocalDateTime으로 매핑함

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
