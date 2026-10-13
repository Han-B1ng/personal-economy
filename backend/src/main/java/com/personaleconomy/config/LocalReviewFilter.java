package com.personaleconomy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personaleconomy.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Restricts the unauthenticated single-user review adapter to local requests. */
@Component
@Profile("(bootstrap | mysql) & !prod & !preview")
public class LocalReviewFilter extends OncePerRequestFilter {
    private static final Set<String> HOSTS=Set.of("localhost","127.0.0.1","::1","[::1]");
    private final ObjectMapper mapper;
    public LocalReviewFilter(ObjectMapper mapper){this.mapper=mapper;}
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws ServletException,IOException {
        if(!request.getRequestURI().startsWith("/api/")){chain.doFilter(request,response);return;}
        boolean local=HOSTS.contains(request.getServerName())&&InetAddress.getByName(request.getRemoteAddr()).isLoopbackAddress();
        String origin=request.getHeader("Origin");
        if(origin!=null){try{URI uri=URI.create(origin);local=local&&uri.getHost()!=null&&uri.getScheme()!=null&&HOSTS.contains(uri.getHost())&&Set.of("http","https").contains(uri.getScheme());}catch(IllegalArgumentException e){local=false;}}
        if(!local){response.setStatus(403);response.setContentType("application/json");response.setCharacterEncoding("UTF-8");mapper.writeValue(response.getOutputStream(),ApiResponse.error("LOCAL_REVIEW_ONLY","此接口仅供本机验收，不支持外部访问"));return;}
        chain.doFilter(request,response);
    }
}
