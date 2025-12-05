package com.green.min.dto;

// 사용자 요청을 Controller까지 운반만 하고 그 외 하는거 없음
// 엔티티, 레포지토리, 서비스, 컨트롤러도 아님. 그럼 뭐냐? [DTO]!!
// Data Transfer Object => [DTO] (데이터 운반 객체) -> 사용자 데이터 운반 할 때만 쓰이는 객체(클래스)라서 DTO라고 불림
public class PostUpdateRequest {
//    private int id;
    private String title;
    private String content;

    public PostUpdateRequest(String title, String content) {

        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
