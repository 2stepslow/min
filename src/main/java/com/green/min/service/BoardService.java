package com.green.min.service;

import com.green.min.dto.PostCreateRequest;
import com.green.min.dto.PostResponse;
import com.green.min.dto.PostUpdateRequest;
import com.green.min.entity.Board;
import com.green.min.entity.User;
import com.green.min.exceptions.AuthenticationFailureException;
import com.green.min.exceptions.AuthorizationFailureException;
import com.green.min.exceptions.ResourceNotFoundException;
import com.green.min.exceptions.ValidationException;
import com.green.min.repository.BoardRepository;
import com.green.min.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    public User getLoginUser(HttpSession session) {
        Object userIdAsString = session.getAttribute(("userId"));
        if (userIdAsString == null) {  // 스프링부트 내부 로직때문에 userId null이면 id를 못찾은것!
            throw new AuthenticationFailureException("로그인이 필요합니다.");
        }

        int userId = Integer.parseInt(userIdAsString.toString()); // 세션 장부에서 지금 로그인 한 사용자 id 추출

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다");
        }

        return optionalUser.get();
    }

    // 1. 게시글 전체 조회
    public List<PostResponse> getAllBoards(HttpSession session) {  // 컨트롤러가 아니기 때문에 responseentity를 리턴 할 수가 없음
        // 로그인 체크
        User user = getLoginUser(session);

        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        List<Board> results = boardRepository.findAll();

        // 새로운 결과 전용 상자 제작
        List<PostResponse> response = new ArrayList<>();

        for (Board board : results) {
            Optional<User> optionalUser = userRepository.findById(board.getAuthor());
            if (optionalUser.isEmpty()) {
                throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
            }
            User author = optionalUser.get();
            String authorName = author.getName();

            PostResponse newResult = new PostResponse(
                    board.getID(),
                    board.getTitle(),
                    board.getContent(),
                    authorName
            );
            response.add(newResult);
        }
        return response;
    }

    // 2. 상세 조회
    public PostResponse getDetailPost(HttpSession session, int id) {
        // 로그인 체크
        User user = getLoginUser(session);

        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        // DB에서 id에 해당하는 row 찾고, 있으면 Board 객체 만들고 Optional에 넣어서 돌려주고, 없으면 빈 Optional 돌려줌
        Optional<Board> boardOptional = boardRepository.findById(id);

        // 값이 없으면(Optional이 비어 있으면) 404 반환
        if (boardOptional.isEmpty()) {  // 상자 비었냐? 먼저 확인
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        // 여기까지 내려왔다는 건 값이 있다는 뜻이니 안전하게 get()
        Board board = boardOptional.get();  // get(): Optional<Board> 상자 안에 있는 Board 객체 꺼내서 board 변수에 넣어라

        // board의 author로 user를 조회해서, 작성자의 이름을 얻어내야 함
        Optional<User> userOptional = userRepository.findById(board.getAuthor());
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
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

        return response;
    }

    // 3. 새 글 작성
    public int createNewPost(HttpSession session, PostCreateRequest request) {
        // 로그인 체크
        User user = getLoginUser(session);

        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        //  || : or  /  && : and
        // 제목 검증   / ( .length: 함수 )
        // if문에 조건이 여러개면 앞에있는거부터 검사. ||앞에 null이 나오면 ||뒤는 검사 안함.
        if (request.getTitle() == null || request.getTitle().length() < 10) {
            throw new ValidationException("제목은 반드시 10자 이상 입력 해야 합니다.");
        }
        //내용 검증
        if (request.getContent() == null || request.getContent().length() < 10) {
            throw new ValidationException("내용은 반드시 10자 이상 입력 해야 합니다.");
        }

        // 2. 받았으면 저장하면 됨
        //우리쪽에서 만든걸 DB에넣는건 어케 씀?
        Board board1 = new Board(  // 창고직원이 작업하기 전 board. 비어있음
                request.getTitle(),
                request.getContent(),
                user.getId()
        );

        board1.setHits(0);  // 처음 글이니 0으로 시작
        board1.setAuthor(user.getId());  // 로그인 한 userId를 author로 저장

        Board newPost = boardRepository.save(board1);  // 창고직원이 작업한 후 갖다준 board. 값 채워져있음
        return newPost.getID();
    }

    // 4. 게시글 수정 (돌려줄게 없음. void)
    public void updateNewPost(HttpSession session, PostUpdateRequest request, int id) {
        // 로그인 체크
        User user = getLoginUser(session);  // getLoginUser(session) 메서드 호출해 현재 로그인 한 유저 가져옴

        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");


        // DB에서 id에 해당하는 글(Board) 찾는데 있는지 없는지 모르니 Optional<Board>로 싸서 반환
        Optional<Board> boardOptional = boardRepository.findById(id);
        if (boardOptional.isEmpty()) {  // 게시글이 없으면 true
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다.");
        }

        // 위에서 값 없는 경우는 걸렀으니, 여기까지 왔으면 값 있는 것.
        Board board = boardOptional.get(); // Optional 상자에 있던 Board 객체 꺼내서 board 변수에 넣음

        // 요청자의 user Id
        int requestUserId = user.getId();

        // 게시글 원 작성자의 user Id
        int writerId = board.getAuthor();

        if (requestUserId != writerId) {  // requestUserId(로그인한사람), writerId(작성자) 다르면 true
            throw new AuthenticationFailureException("권한이 없습니다.");
        }

        //--------------------??
        // 수정할 필드만 선택적으로 업데이트 (null이 아닌 것만 반영)
        if (request.getContent() != null) {  // null 아니면 수정했다는 뜻 (JSON에서 content 안보냈으면 getContent()는 null)
            // DB UPDATE 해야 됨
            board.setContent(request.getContent());  // Board엔티티의 content필드를 새로운 값으로 바꿈
        }

        if (request.getTitle() != null) {  // null 아니면 수정했다는 뜻
            board.setTitle(request.getTitle());
        }
        //--------------------??
    }

    // 5. 글 삭제
    public void deletePost(HttpSession session, int id) {
        // 로그인 체크
        User user = getLoginUser(session);

        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        // 요청자가 게시글 작성자와 일치하는지 확인
        int requestUserId = user.getId();

        Optional<Board> boardOptional = boardRepository.findById(id);
        if (boardOptional.isEmpty()) {
            throw new ResourceNotFoundException("게시물 못찾음");
        }
        Board board = boardOptional.get();
        int writerId = board.getAuthor();

        // 요청자 ID와 게시글 작성자 ID가 다르면 삭제를 거절한다.
        if (requestUserId != writerId) {
            throw new AuthorizationFailureException("작성자 외에는 삭제 불가");
        }

        boardRepository.deleteById(id);
    }

    // 6. 내가 작성한 글 조회  /TODO 군데군데 고쳐야 함!!! 지금은 DB에서 이것저것 다 꺼내서 확인하는 방식(일너무많이함)
    public List<PostResponse> getMyPost(HttpSession session) {
        User user = getLoginUser(session);

        if (user == null) {
            throw new AuthenticationFailureException("로그인이 필요합니다");
        }
        System.out.println("안녕하세요" + user.getName() + "님. 당신의 요청은 허용되었습니다.");

        List<Board> results = boardRepository.findAll();  // ??

        // 새로운 결과 전용 상자 제작
        List<PostResponse> response = new ArrayList<>();

        for (Board board : results) {
            Optional<User> optionalUser = userRepository.findById(board.getAuthor());
            if (optionalUser.isEmpty()) {
                throw new ResourceNotFoundException("유저를 찾을 수 없습니다.");
            }
            if (board.getAuthor() == user.getId()) {
                User author = optionalUser.get();
                String authorName = author.getName();

                PostResponse newResult = new PostResponse(
                        board.getID(),
                        board.getTitle(),
                        board.getContent(),
                        authorName
                );
                response.add(newResult);
            }
        }
        return response;
    }
}


//}

// list<PostResponse>로 작동하도록 변경