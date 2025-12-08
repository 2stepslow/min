package com.green.min.controller;

import com.green.min.dto.PostCreateRequest;
import com.green.min.dto.PostResponse;
import com.green.min.dto.PostUpdateRequest;
import com.green.min.entity.Board;
import com.green.min.entity.User;
import com.green.min.repository.BoardRepository;
import com.green.min.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController  // 이 어노테이션을 붙여야, 요청을 받을 수 있음
@RequestMapping("/api/board")  // api board 관련 요청만 처리해라 (역할 분배)
public class BoardController {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardController(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }
    // 서버와 DB가 기본적으로 연결 되어 있어야 됨. 안되어있으면 이하 작업 모두 할 수 없음.

    // 1. 세션에서 userId를 찾으면 -> 정상 유저
    // 2. 세션에서 userId를 못 찾으면 -> 비정상 유저
    // 그래서 이 메서드는, 정상 유저면 userId로 userDB에서 찾아서 User 객체를 내려줌
    // 비정상 유저면, 그냥 null을 내려줌
    public User getLoginUser(HttpSession session) {
        Object userIdAsString = session.getAttribute(("userId"));
        if(userIdAsString == null) {  // 스프링부트 내부 로직때문에 userId null이면 id를 못찾은것!
            return null;  // 비정상 유저일 경우 null 반환
        }
        int userId = Integer.parseInt(userIdAsString.toString()); // 세션 장부에서 지금 로그인 한 사용자 id 추출

        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) {
            System.out.println("이런 유저 없음");
            return null;  // 유저 못 찾았으면 null
        }

        return optionalUser.get();
    }


    // 1. 모든 게시글을, 작성 최신순으로 조회.
//    @GetMapping("/getAllBoards")  // 외부에서 getAllBoards라고 요청하는 유저들은 다 여기로 오겠구나 하면 됨
    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards(HttpSession session) {   // url하나당 메서드 하나로 매핑! (댓글 다는것과 게시글 작성은 코드도 다르고 DB테이블도 다름)
        // 로그인 체크
        User user = getLoginUser(session);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 괄호 안에 401 넣어도 되고 이렇게 써도 됨
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");


        /* FindAll: 스프링 개발자가 만들어 둔 것.
        [FindAll의 내부 동작]
        1. boardRepository -> Board -> @Table에서 어느 테이블인지 알 수 있음
        2. application.properties에서 DB 접속정보 알 수 있음
        위 두 정보를 종합해 우리 DB의 boards 테이블로 가서 정해진 SELECT ALL 쿼리를 날림
        그렇게 받은 쿼리 결과들을 Java의 Board 클래스로 전부 변환함
        SELECT ALL은 다수의 결과이기 때문에 List<엔티티클래스> 형태로 반환함 */
        List<Board> results = boardRepository.findAll();   // DB 테이블의 모든 행(Row)을 조회하여 Board 객체로 변환(매핑)한 뒤 리스트에 담는다.
        return ResponseEntity.ok(results);  // 200 OK with 글 데이터들
    }

    // 2. 상세 조회. 게시글 클릭했을 때, 그 글의 상세 정보 줘야 함.
    // GET인 경우 RequestBody 사용하면 안되는 규칙 꼭 지키기
    // 데이터는 URL에 포함시켜 보내기  ex.green.tistory.com/81
    // /81, /99 등 바뀔수 있으니 URL은 특정 숫자가 아닌, {id}로 설정
    // PathVariable 설정까지 해주면, 스프링이 URL에서 숫자 뽑아 int id로 알아서 갖다줌
//    @GetMapping("getDetail/{id}")  // 상세조회도 '조회'임 / getDetail 뒤에 아무숫자 들어오면 이 url로 들어오라고 하는 것.
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getDetail(@PathVariable int id, HttpSession session) {

        // 로그인 체크
        User user = getLoginUser(session);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 괄호 안에 401 넣어도 되고 이렇게 써도 됨
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        // DB에서 id에 해당하는 row 찾고, 있으면 Board 객체 만들고 Optional에 넣어서 돌려주고, 없으면 빈 Optional 돌려줌
        Optional<Board> boardOptional = boardRepository.findById(id);

        // 값이 없으면(Optional이 비어 있으면) 404 반환
        if (boardOptional.isEmpty()) {  // 상자 비었냐? 먼저 확인
            return ResponseEntity.notFound().build();  // 비었으면 여기서 끝내 (return)
        }

        // 여기까지 내려왔다는 건 값이 있다는 뜻이니 안전하게 get()
        Board board = boardOptional.get();  // get(): Optional<Board> 상자 안에 있는 Board 객체 꺼내서 board 변수에 넣어라

        // board의 author로 user를 조회해서, 작성자의 이름을 얻어내야 함
        Optional<User> userOptional = userRepository.findById(board.getAuthor());
        if(userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User writer = userOptional.get();
        String writerName = writer.getName();  // <- 글 작성자의 이름 가져왔음

        // DTO 만들어서, DTO 응답해야 함
        PostResponse response = new PostResponse(
                board.getID(),
                board.getTitle(),
                board.getContent(),
                writerName
        );

        // 조회수 늘려주는 로직: '조회'랑은 관련 없음
        board.setHits(board.getHits() + 1); // 조회수 1 늘리기
        boardRepository.save(board); // 변경된 조회수 데이터를 반영해서 다시 저장

        return ResponseEntity.ok(response);              //  <==== 여기 근방에 뭘 추가 해야 됨... DTO응답관련
    }

    // 3. 새로운 글 작성
    @PostMapping
    public ResponseEntity<Void> createNewPost(@RequestBody PostCreateRequest request, HttpSession session) {

        // 로그인 체크
        User user = getLoginUser(session);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 괄호 안에 401 넣어도 되고 이렇게 써도 됨
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");


        // 1. 사용자가 입력해서 보낸 데이터를 받아야 함
        System.out.println(request);

        System.out.println(request.getContent());
        System.out.println(request.getTitle());

        // 2. 받았으면 저장하면 됨
        //우리쪽에서 만든걸 DB에넣는건 어케 씀?
        Board board1 = new Board(  // 창고직원이 작업하기 전 board. 비어있음
                request.getTitle(),
                request.getContent()
        );

        board1.setHits(0);  // 처음 글이니 0으로 시작
        board1.setAuthor(user.getId());  // 로그인 한 userId를 author로 저장

        Board board = boardRepository.save(board1);  // 창고직원이 작업한 후 갖다준 board. 값 채워져있음
        int newPostId = board.getID();
        URI location = URI.create("/getDetail." + newPostId);  // 클라이언트한테 이 자원이 어디 생성되었는지를 말해주는 것.

        return ResponseEntity.created(null).build();
        // location: 어느 URL에서 확인할수 있냐 -> 등록버튼 누르면 네이버서버로 가고 201created, 등록되면 id가 부여됨(n번째게시글). 프론트가 n번째글(지금작성한거)의 상세내용으로 페이지 이동됨. => 내부에선 이렇게 작동됨
    }

    // 4. 수정
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updatePost(  // ResponseEntity: HTTP 응답 담는 포장지 / <Void>: 이 응답은 바디(내용물) 없음
            @PathVariable int id,
            @RequestBody PostUpdateRequest request,  // HTTP body(JSON등)의 내용을 PostUpdateRequest라는 DTO형태로 받아옴
            HttpSession session  // 현재 요청 보낸 사람의 세션 정보 / session.getAttribute("userId")로 누가 로그인했는지 확인할 수 있음
    ) {

        // 로그인 체크
        User user = getLoginUser(session);  // getLoginUser(session) 메서드 호출해 현재 로그인 한 유저 가져옴
        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 괄호 안에 401 넣어도 되고 이렇게 써도 됨
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");


        // 요청자가 게시글 작성자와 일치하는지 확인
        int requestUserId = user.getId();  // User 엔티티의 getter 이용해 로그인 한 user의 id값 꺼내서 requestUserId 변수에 저장

        // DB에서 id에 해당하는 글(Board) 찾는데 있는지 없는지 모르니 Optional<Board>로 싸서 반환
        Optional<Board> boardOptional = boardRepository.findById(id);
        if(boardOptional.isEmpty()) {  // 게시글이 없으면 true
            return ResponseEntity.notFound().build();
        }

        // 위에서 값 없는 경우는 걸렀으니, 여기까지 왔으면 값 있는 것.
        Board board = boardOptional.get(); // Optional 상자에 있던 Board 객체 꺼내서 board 변수에 넣음
        int writerId = board.getAuthor(); // Board의 author필드 getter로 꺼내 writerId 변수에 넣음

        if(requestUserId != writerId) {  // requestUserId(로그인한사람), writerId(작성자) 다르면 true
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // 401(UNAUTHORIZED) 아니고 403(FORBIDDEN) 던지기
        }

        // 수정할 필드만 선택적으로 업데이트 (null이 아닌 것만 반영)
        if (request.getContent() != null) {  // null 아니면 수정했다는 뜻 (JSON에서 content 안보냈으면 getContent()는 null)
            // DB UPDATE 해야 됨
            board.setContent(request.getContent());  // Board엔티티의 content필드를 새로운 값으로 바꿈
        }

        if (request.getTitle() != null) {  // null 아니면 수정했다는 뜻
            board.setTitle(request.getTitle());
        }

        // 변경 내용을 DB에 반영
        boardRepository.save(board);  // 이미 존재하는 Board 엔티티를 저장하므로, JPA입장에서는 UPDATE에 해당

        // 바디 없는 200 OK 응답 ('ResponseEntity<Void>'이기 때문)
        return ResponseEntity.ok().build();  // .ok: status code 200 / .build(): 이 설정대로 응답 객체를 완성해서 돌려줘
    }


    // 5. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable int id, HttpSession session) {

        // 로그인 체크
        User user = getLoginUser(session);
        if(user == null) {
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();  // 괄호 안에 401 넣어도 되고 이렇게 써도 됨
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        // 요청자가 게시글 작성자와 일치하는지 확인
        int requestUserId = user.getId();

        Optional<Board> boardOptional = boardRepository.findById(id);
        if(boardOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Board board = boardOptional.get();
        int writerId = board.getAuthor();

        // 요청자 ID와 게시글 작성자 ID가 다르면 삭제를 거절한다.
        if(requestUserId != writerId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // 401(UNAUTHORIZED) 아니고 403(FORBIDDEN) 던지기
        }

        boardRepository.deleteById(id);
        return ResponseEntity.noContent().build();  // 204 코드 반환
    }

    // [DB]
    // 1. DB 연결 하고싶음
    // 2. 연결할 DB 있어야 함
    // 3. 연결할 테이블 있어야 함
    // 4. 테이블 설계부터 해야 하네!

    // 작성자, 제목, 내용
}

