package com.green.min.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test")
public class Test {
    @Id
    @GeneratedValue
    private int idtest;

    @Column(name = "content")
    private String content;

    public Test() {}

    public Test(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
