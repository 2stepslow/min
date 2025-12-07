package com.green.min.entity;

import jakarta.persistence.*;

// Entity: JPA가 DB에서 가져온 데이터를 최초로 넣는 곳
@Entity
// JPA가 어느 테이블로 찾아가야 하는지 알려주는 곳
@Table(name = "boards")  // <- MySQL 테이블 이름. 이 클래스와 mysql의 boards 테이블 매칭
public class Board {
    // 필드
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


    // 기본 생성자: 필드 초기화 없는 생성자를 뜻함
    // 우리가 아니라 JPA가 쓸거임
    public Board() {}


    // 일반 생성자 -> 우리가 쓸거임
    public Board(String title, String content) {
        this.title = title;
        this.content = content;
    }


    // Getter (private이므로)
    public int getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    // Setter (id는 setter로 두면 안됨!!!!!!@!!$)

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public int getAuthor() {
        return author;
    }

    public void setAuthor(int author) {
        this.author = author;
    }
}
