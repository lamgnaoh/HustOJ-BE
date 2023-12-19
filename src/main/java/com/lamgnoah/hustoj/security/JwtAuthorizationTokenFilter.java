package com.lamgnoah.hustoj.security;

import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;
  private final JwtTokenUtil jwtTokenUtil;

  @Value("${jwt.header}")
  private  String tokenHeader;

  private final UserRepository userRepository;


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    log.debug("processing authentication for '{}'", request.getRequestURL());

    String requestHeader = request.getHeader(this.tokenHeader);
    boolean saved = false;
    String username = null;
    String authToken = null;
    if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
      authToken = requestHeader.substring(7);
      try {
        username = jwtTokenUtil.getUsernameFromToken(authToken);
      } catch (IllegalArgumentException e) {
        log.error("an error occured during getting username from token", e);
      } catch (ExpiredJwtException e) {
        log.warn("the token is expired and not valid anymore", e);
      }
    }

    log.debug("checking authentication for user '{}'", username);
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      log.debug("security context was null, so authorizating user");

      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
      if (jwtTokenUtil.validateToken(authToken, userDetails)) {
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        log.info("authorizated user '{}', setting security context", username);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = request.getHeader("Authorization").substring(7);
        String usernameFromToken = jwtTokenUtil.getUsernameFromToken(token);
        User user = userRepository.findByUsername(usernameFromToken);
        try (UserContext ignored = new UserContext(user)) {
          log.info("authorizated user '{}', saving security context", username);
          saved = true;
          chain.doFilter(request, response);
        }
      }
    }
    if (!saved) {
      chain.doFilter(request, response);
    }

  }
}
