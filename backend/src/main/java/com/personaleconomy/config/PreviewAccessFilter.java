package com.personaleconomy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personaleconomy.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Only accepts API requests forwarded by the authenticated private review gateway. */
@Component
@Profile("preview & !prod")
public class PreviewAccessFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper;
    private final byte[] token;
    private final String origin;

    public PreviewAccessFilter(ObjectMapper mapper,
            @Value("${personal-economy.preview-proxy-token}") String token,
            @Value("${personal-economy.preview-origin}") String origin) {
        if (token.length() < 32) throw new IllegalArgumentException("Preview gateway token is too short");
        this.mapper = mapper;
        this.token = token.getBytes(StandardCharsets.UTF_8);
        this.origin = canonicalOrigin(origin);
    }

    private static String canonicalOrigin(String value) {
        URI uri = URI.create(value);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || !(uri.getPath().isEmpty() || "/".equals(uri.getPath()))) {
            throw new IllegalArgumentException("Preview origin must be an HTTPS origin");
        }
        int port = uri.getPort();
        return "https://" + uri.getHost().toLowerCase(java.util.Locale.ROOT)
                + (port == -1 || port == 443 ? "" : ":" + port);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        String provided = request.getHeader("X-PE-Review-Token");
        boolean allowed = provided != null && MessageDigest.isEqual(token,
                provided.getBytes(StandardCharsets.UTF_8));
        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin != null) {
            try { allowed &= origin.equals(canonicalOrigin(requestOrigin)); }
            catch (IllegalArgumentException exception) { allowed = false; }
        }
        allowed &= !"cross-site".equalsIgnoreCase(request.getHeader("Sec-Fetch-Site"));
        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            mapper.writeValue(response.getOutputStream(), ApiResponse.error(
                    "PREVIEW_ACCESS_DENIED", "当前请求无法访问测试工作区"));
            return;
        }
        chain.doFilter(request, response);
    }
}
