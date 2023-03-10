package com.marizoo.user.controller;

import com.marizoo.user.api.*;
import com.marizoo.user.dto.BookDto;
import com.marizoo.user.dto.BadgeDto;
import com.marizoo.user.dto.FavorStoreDto;
import com.marizoo.user.dto.JoinRequestDto;
import com.marizoo.user.entity.User;
import com.marizoo.user.dto.ExceptionResponseDto;
import com.marizoo.user.exception.AlreadyJoinException;
import com.marizoo.user.exception.PasswordNotMatchException;
import com.marizoo.user.exception.RefreshTokenException;
import com.marizoo.user.exception.UserNotFoundException;
import com.marizoo.user.repository.UserRepository;
import com.marizoo.user.service.AuthService;
import com.marizoo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static com.marizoo.user.constant.JwtConstant.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/")
public class UserController {

    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/users")
    public ResponseEntity join(@Valid @RequestBody JoinRequestDto joinRequestDto) {

        if (userService.isDuplicatedEmail(joinRequestDto.getEmail())) {
            throw new AlreadyJoinException("이미 가입된 이메일입니다.");
        }

        User user = new User();
        user.setUid(joinRequestDto.getUid());
        user.setPwd(encoder.encode(joinRequestDto.getPwd()));
        user.setNickname(joinRequestDto.getNickname());
        user.setPhoneNumber(joinRequestDto.getPhoneNumber());
        user.setEmail(joinRequestDto.getEmail());
        user.setRole("ROLE_USER");

        userRepository.save(user);

        return ResponseEntity.status(HttpServletResponse.SC_CREATED).build();
    }

    @GetMapping("/refresh")
    public ResponseEntity refresh(@CookieValue(RT_HEADER) String refreshToken, HttpServletResponse response) {
        log.info("refresh method 입장");
        Map<String, String> tokenMap = authService.refresh(refreshToken, response);

        response.addHeader(AT_HEADER, tokenMap.get(AT_HEADER));
        if (tokenMap.get(RT_HEADER) != null) {
            // refresh token이 재생성되었으므로 쿠키에 저장하여 보내주어야한다.
            ResponseCookie cookie = ResponseCookie.from(RT_HEADER, refreshToken)
                    .httpOnly(true)
                    .domain("i8b208.p.ssafy.io")
                    .path("/api/user/refresh")
                    .sameSite("None")
                    .secure(true)
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/check-uid")
    public ResponseEntity uidDuplicatedCheck(@RequestParam String uid) {
        return userService.isDuplicatedUid(uid) ? ResponseEntity.ok().build() : ResponseEntity.status(HttpServletResponse.SC_CONFLICT).build();
    }

    @GetMapping("/users/check-nickname")
    public ResponseEntity nicknameDuplicatedCheck(@RequestParam String nickname) {
        return userService.isDuplicatedNickname(nickname) ? ResponseEntity.ok().build() : ResponseEntity.status(HttpServletResponse.SC_CONFLICT).build();
    }

    @GetMapping("/users/find-uid")
    public ResponseEntity findUidByEmail(@RequestParam String email) {
        String uid = userService.findUidByEmail(email);
        return ResponseEntity.ok(new FindUidResponseApi(uid));
    }

    @GetMapping("/users/find-pwd")
    public ResponseEntity findPwdByEmail(@RequestParam String email) {
        userService.createMailAndChangePwd(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity myPage(@RequestBody Map<String, String> request, @PathVariable Long userId) {
        String pwd = request.get("pwd");
        MyPageResponseApi myPageInfo = userService.getMyPageInfo(userId, pwd);
        return ResponseEntity.ok(myPageInfo);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity modifyMyPage(@RequestBody MyPageRequestApi myPageRequest, @PathVariable Long userId) {
        userService.modifyMyPageInfo(userId, myPageRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{userId}/change-pwd")
    public ResponseEntity changePwd(@RequestBody PwdChangeRequestApi request, @PathVariable Long userId) {
        userService.changePwd(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/stores")
    public ResponseEntity getFavorStoreList(@PathVariable Long userId) {
        List<FavorStoreDto> favorStoreList = userService.getFavorStoreList(userId);
        return ResponseEntity.ok(new FavorStoreListResponseApi(favorStoreList));
    }

    @GetMapping("/users/{userId}/books")
    public ResponseEntity getBookList(@PathVariable Long userId) {
        List<BookDto> bookDtoList = userService.getBookList(userId);
        return ResponseEntity.ok(new BookListResponseApi(bookDtoList));
    }

    @DeleteMapping("users/{userId}/books/{bookId}")
    public ResponseEntity deleteBook(@PathVariable Long userId, @PathVariable Long bookId) {
        userService.deleteBook(userId, bookId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcasts/badges")
    public ResponseEntity addBadgeAtRelatedUsers(@RequestBody BulkBadgeRequestApi bulkBadgeRequest) {
        userService.bulkAddBadge(bulkBadgeRequest.getUserIdList(), bulkBadgeRequest.getBadgeId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/badges")
    public ResponseEntity getBadgeList(@PathVariable Long userId) {
        List<BadgeDto> badgeList = userService.getBadgeList(userId);
        return ResponseEntity.ok(new MyPageBadgeListResponseApi(badgeList));
    }

    @PutMapping("/users/watchEnd")
    public ResponseEntity updateCountAndWatchTimeAcc(@RequestBody WatchEndRequestApi watchEndRequestApi) {
        userService.updateCountAndWatchTimeAcc(watchEndRequestApi);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}/get-nickname")
    public ResponseEntity getNickname(@PathVariable Long userId) {
        User user = userRepository.findById(userId).get();
        return ResponseEntity.ok(new NickNameResponseApi(user.getNickname()));
    }

    // Exception
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(RefreshTokenException.class)
    public ExceptionResponseDto refreshTokenException(RefreshTokenException e) {
        return new ExceptionResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(AlreadyJoinException.class)
    public ExceptionResponseDto alreadyJoinException(AlreadyJoinException e) {
        return new ExceptionResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserNotFoundException.class)
    public ExceptionResponseDto userNotFoundException(UserNotFoundException e) {
        return new ExceptionResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PasswordNotMatchException.class)
    public ExceptionResponseDto passwordNotMatchException(PasswordNotMatchException e) {
        return new ExceptionResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ExceptionResponseDto incorrectResultSizeDataAccessException(IncorrectResultSizeDataAccessException e) {
        return new ExceptionResponseDto(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ExceptionResponseDto methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.toString());
        return new ExceptionResponseDto(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public ExceptionResponseDto exception(Exception e) {
        log.error("회원가입 validation check exception");
        log.error("class = {} message = {}", e.getClass(), e.getMessage());
        return new ExceptionResponseDto("잘못된 요청입니다.");
    }
}
