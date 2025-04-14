package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AuthTokenFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            logger.info("Processing request to: {}", request.getRequestURI());
            String jwt = parseJwt(request);
            
            if (jwt != null) {
                logger.info("JWT token found in request");
                boolean isValid = jwtUtils.validateJwtToken(jwt);
                logger.info("JWT token validation result: {}", isValid);
                
                if (isValid) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.info("Username extracted from token: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("User details loaded for: {}", username);
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("Authentication set in SecurityContextHolder");
                }
            } else {
                logger.info("No JWT token found in request");
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            logger.error("Authentication error details", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        logger.info("Authorization header: {}", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.info("Extracted token from header: {}", token.substring(0, Math.min(10, token.length())) + "...");
            return token;
        }

        return null;
    }
}
