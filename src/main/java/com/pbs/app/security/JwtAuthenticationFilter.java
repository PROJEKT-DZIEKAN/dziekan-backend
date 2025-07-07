package com.pbs.app.security;

import com.pbs.app.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JWTService jwtService;

  public JwtAuthenticationFilter(JWTService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      String userId = jwtService.extractUserId(token);
      if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        var auth = new UsernamePasswordAuthenticationToken(
          userId,
          null,
          Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
      }
    }
    filterChain.doFilter(request, response);
  }
  @Override
protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/") ||
           path.startsWith("/api/qr/") ||
           path.equals("/api/refresh-token") ||
           path.startsWith("/swagger-ui") ||
          path.startsWith("/swagger-ui/index.html") ||
           path.startsWith("/v3/api-docs");
}

}
