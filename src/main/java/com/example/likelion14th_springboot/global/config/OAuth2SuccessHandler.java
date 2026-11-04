package com.example.likelion14th_springboot.global.config;

import com.example.likelion14th_springboot.domain.Member;
import com.example.likelion14th_springboot.jwt.JwtTokenProvider;
import com.example.likelion14th_springboot.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        // email로 우리 서비스의 회원을 찾고, 토큰에는 일반 로그인과 같이 name을 넣는다
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        String accessToken = jwtTokenProvider.generateAccessToken(member.getName());

        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:8080/oauth2/success") //프론트와 협업이라면 프론트 주소를 넣어줘야함.
                .queryParam("accessToken", accessToken)
                .build()
                .encode()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
