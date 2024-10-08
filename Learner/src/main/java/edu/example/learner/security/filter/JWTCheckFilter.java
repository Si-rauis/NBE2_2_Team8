package edu.example.learner.security.filter;

import edu.example.learner.security.auth.CustomUserPrincipal;
import edu.example.learner.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@RequiredArgsConstructor
@Log4j2
public class JWTCheckFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("--- doFilterInternal() ");
        log.info("--- requestURI : " + request.getRequestURI());


        boolean isPublicPath = false;

        // 요청 URI 및 HTTP 메소드 확인
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        log.info("method : " + method);
        log.info("requestURI : " + requestURI);


        if((request.getMethod().equals("GET") && (
                (   requestURI.matches("/course/\\d+") ||
                    requestURI.matches("/course/\\d+/member-nickname") ||
                    requestURI.matches("/course/video/\\d+") ||
                    requestURI.matches("/course/list") ||
                    requestURI.matches("/members/other/\\w+") ||
                    requestURI.matches("/members/instructor/\\w+")||
                    requestURI.matches("/course/\\d+/news/\\d+") ||
                    requestURI.matches("/course/\\d+/news")   ||
                    requestURI.matches("/members/find/.*")  ||
                    requestURI.matches("/join/*") ||
                    requestURI.matches("/members/instructor/\\w+/reviews/list") ||
                    requestURI.matches("/inquiries")    ||
                    requestURI.matches("/course/\\d+/reviews/list") ||
                    requestURI.matches("/course/\\d+/course-inquiry/\\d+") ||
                    requestURI.matches("/course/\\d+/course-inquiry") ||
                    requestURI.matches("/course/\\d+/course-answer/\\d+")
                )
        )))
        {
            log.info("JWT check passed");
            isPublicPath = true;
        }

        if (isPublicPath) {
            filterChain.doFilter(request, response);
            return;
        }


        // Authorization 쿠키에서 토큰 추출
        String authorization = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("cookie : " + cookie.getName());
                if (cookie.getName().equals("Authorization")) {
                    authorization = cookie.getValue();
                }
            }
        } else {
            log.info("--- No cookies found");
        }

        log.info("--- authorization : " + authorization);

        if (authorization == null) {
            handleException(response, new Exception("ACCESS TOKEN NOT FOUND"));
            return;
        }

        // 토큰 유효성 검증
        String accessToken = authorization;

        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);
            log.info("--- 토큰 유효성 검증 완료 ---");

            // SecurityContext 처리
            String mid = claims.get("mid").toString();
            String role = claims.get("role").toString(); // 단일 역할 처리
            log.info("권한 : " + role);

            log.info("권한 : " + role);
            // 토큰을 이용하여 인증된 정보 저장
            UsernamePasswordAuthenticationToken authToken
                    = new UsernamePasswordAuthenticationToken(new CustomUserPrincipal(mid, role), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            log.info("authToken : " + authToken);

            // SecurityContext에 인증/인가 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.info("SecurityContext에 인증/인가 정보 저장");

            // OAuth2 인증 생략, 다음 필터로 요청 전달
            filterChain.doFilter(request, response);
            log.info("end");
        } catch (Exception e) {
            handleException(response, e); // 예외 발생 시 처리
        }
    }


    public void handleException(HttpServletResponse response, Exception e) throws IOException {
        log.info("--- handleException ---");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
    }
}
