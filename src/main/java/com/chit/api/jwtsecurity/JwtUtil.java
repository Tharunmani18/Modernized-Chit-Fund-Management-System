package com.chit.api.jwtsecurity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

  private final String secret;
  private final long expiration;

  public JwtUtil(@Value("${jwt.secret}") String secret,
      @Value("${jwt.expiration}") long expiration) {
    this.secret = secret;
    this.expiration = expiration;
  }

  public String extractUsername(String token) {
    log.info("Entered extract username method...");
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    log.info("Entered extract expiration method...");
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    log.info("Entered extract claim method...");
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    log.info("Entered extract all claims method...");
    return Jwts.parserBuilder()
        .setSigningKey(secret)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  private Boolean isTokenExpired(String token) {
    log.info("Entered is token expired method...");
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(String username) {
    log.info("Entered generate token method...");
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();
  }

  public Boolean validateToken(String token, String username) {
    log.info("Entered validate token method...");
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username) && !isTokenExpired(token));
  }
}
