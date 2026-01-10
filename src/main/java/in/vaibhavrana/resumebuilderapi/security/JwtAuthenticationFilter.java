package in.vaibhavrana.resumebuilderapi.security;

import in.vaibhavrana.resumebuilderapi.document.User;
import in.vaibhavrana.resumebuilderapi.repository.UserRespository;
import in.vaibhavrana.resumebuilderapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRespository userRespository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/verify-email")
                || path.equals("/api/auth/resend-verification")
                || path.equals("/api/auth/upload-image");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            try {
                String userId = jwtUtil.getUserIdFromToken(token);

                if (userId != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null &&
                        !jwtUtil.isTokenExpired(token) &&
                        jwtUtil.validateToken(token)) {

                    User user = userRespository.findById(userId)
                            .orElseThrow(() ->
                                    new UsernameNotFoundException("User not found"));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user, null, new ArrayList<>());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                log.error("JWT authentication failed", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
