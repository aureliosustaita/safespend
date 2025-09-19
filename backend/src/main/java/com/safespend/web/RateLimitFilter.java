package com.safespend.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(10)
public class RateLimitFilter extends OncePerRequestFilter {

  // allow 5 POSTs per 10 seconds per principal (username); fall back to IP if unknown
  private static final int LIMIT = 5;
  private static final long WINDOW_MS = 10_000L;

  private final Map<String, ArrayDeque<Long>> buckets = new ConcurrentHashMap<>();

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !("POST".equalsIgnoreCase(request.getMethod()) &&
             request.getRequestURI().startsWith("/api/transactions"));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String user = (req.getUserPrincipal() != null) ? req.getUserPrincipal().getName()
                : (req.getRemoteAddr() != null ? req.getRemoteAddr() : "anon");
    long now = System.currentTimeMillis();
    ArrayDeque<Long> q = buckets.computeIfAbsent(user, k -> new ArrayDeque<>());

    // purge old timestamps
    while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
      q.pollFirst();
    }

    if (q.size() >= LIMIT) {
      res.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      res.setContentType("application/json");
      res.getWriter().write("{\"error\":\"rate_limited\",\"detail\":\"Too many requests. Try again in a few seconds.\"}");
      return;
    }

    q.addLast(now);
    chain.doFilter(req, res);
  }
}
