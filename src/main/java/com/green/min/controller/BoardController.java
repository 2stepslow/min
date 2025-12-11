package com.green.min.controller;

import com.green.min.dto.PostCreateRequest;
import com.green.min.dto.PostResponse;
import com.green.min.dto.PostUpdateRequest;
import com.green.min.exceptions.AuthenticationFailureException;
import com.green.min.exceptions.AuthorizationFailureException;
import com.green.min.exceptions.ResourceNotFoundException;
import com.green.min.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController  // 이 어노테이션을 붙여야, 요청을 받을 수 있음
@RequestMapping("/api/board")  // api board 관련 요청만 처리해라 (역할 분배)
public class BoardController {
    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }
    // 서버와 DB가 기본적으로 연결 되어 있어야 됨. 안되어있으면 이하 작업 모두 할 수 없음.


    // 1. 게시글 조회 API
    @GetMapping
    public ResponseEntity<?> getAllBoards(HttpSession session) {  // 얘가 내려갈지 쟤가 내려갈지 잘 모르겠다: <?>사용
        try {
            List<PostResponse> results = boardService.getAllBoards(session);
            return ResponseEntity.ok(results);  // 200 OK with 글 데이터들
        } catch (AuthenticationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

        }
    }


    // 2. 글 상세조회 API
    /// GET인 경우 RequestBody 사용하면 안되는 규칙 꼭 지키기
    // 데이터는 URL에 포함시켜 보내기  ex.green.tistory.com/81
    // /81, /99 등 바뀔수 있으니 URL은 특정 숫자가 아닌, {id}로 설정
    /// PathVariable 설정까지 해주면, 스프링이 URL에서 숫자 뽑아 int id로 알아서 갖다줌
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable int id, HttpSession session) {
        try {
            PostResponse response = boardService.getDetailPost(session, id);
            return ResponseEntity.ok(response);
        } catch (AuthenticationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // 3. 새글작성 API
    @PostMapping
    public ResponseEntity<?> createNewPost(@RequestBody PostCreateRequest request, HttpSession session) {
        try {
            int newPostId = boardService.createNewPost(session, request);
            URI location = URI.create("/getDetail." + newPostId);
            return ResponseEntity.created(null).build();
        } catch(AuthenticationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (AuthorizationFailureException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // 4. 수정 API
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePost(  // ResponseEntity: HTTP 응답 담는 포장지 / <Void>: 이 응답은 바디(내용물) 없음
        @PathVariable int id,
        @RequestBody PostUpdateRequest request,  // HTTP body(JSON등)의 내용을 PostUpdateRequest라는 DTO형태로 받아옴
        HttpSession session  // 현재 요청 보낸 사람의 세션 정보 / session.getAttribute("userId")로 누가 로그인했는지 확인할 수 있음
    ) {
        try {
            // 변경 내용을 DB에 반영. / 수정은 돌려줄게 없음.
            boardService.updateNewPost(session, request, id);  // 이미 존재하는 Board 엔티티를 저장하므로, JPA입장에서는 UPDATE에 해당
            // 바디 없는 200 OK 응답 ('ResponseEntity<Void>'이기 때문)
            return ResponseEntity.ok().build();  // .ok: status code 200 / .build(): 이 설정대로 응답 객체를 완성해서 돌려줘
        } catch (AuthenticationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }


    // 5. 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable int id, HttpSession session) {
        try {
            boardService.deletePost(session, id);
            return ResponseEntity.noContent().build();  // 204 코드 반환
        } catch(AuthenticationFailureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch(ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch(AuthorizationFailureException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


    // 6. 내가 작성한 글 조회
    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyPosts(HttpSession session) {
       try {
           return ResponseEntity.ok(boardService.getMyPost(session));
       } catch (AuthorizationFailureException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
       }
    }



    // [DB]
    // 1. DB 연결 하고싶음
    // 2. 연결할 DB 있어야 함
    // 3. 연결할 테이블 있어야 함
    // 4. 테이블 설계부터 해야 하네!

    // 작성자, 제목, 내용
}

