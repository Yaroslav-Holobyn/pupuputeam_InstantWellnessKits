package com.pupuputeam.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import java.util.function.Function;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;


@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String sercret;
    public String generateToken(String userEmail) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                //valid for 24 hour
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 3600 * 24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(sercret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String getUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    public boolean isTokenValid(String token, String userEmail) {
        final String gotName = getUserEmail(token);
        return  gotName.equals(userEmail) && !isTokenExpired(token);
    }


}
