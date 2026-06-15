package com.kuit.baemin.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

/**
 * Redis에 저장하는 RefreshToken. Redis key = "refreshToken:{memberId}".
 *
 * <p>회원당 1개만 보관(@Id = memberId)한다. 재발급 때 새 값으로 교체(회전)하고,
 * 클라이언트가 보낸 값과 저장된 값이 다르면 '이미 폐기된 옛 토큰의 재사용'으로 보고 탈취를 의심한다.
 * TTL(@TimeToLive)이 지나면 Redis가 알아서 키를 삭제 → 만료된 refresh는 자동 정리된다.
 */
@Getter
@AllArgsConstructor                       // (memberId, token, ttlSeconds)를 받는 생성자 — 저장(save)할 때 사용
@NoArgsConstructor
@RedisHash("refreshToken")                // 이 객체를 Redis 해시로 저장. 키 접두사 = "refreshToken"
public class RefreshToken {

    @Id                               // 이 필드 = Redis 엔티티의 '식별자'. org.springframework.data.annotation.Id (JPA의 PK 애노테이션과 다름). 값이 Redis 키로 들어감 → keyspace:id = refreshToken:{memberId}
    private Long memberId;            // Redis key의 일부 (refreshToken:{memberId}). 코드 일관성 위해 Long — Redis 키 문자열 변환은 Spring Data가 처리

    private String token;             // 현재 유효한 refresh 토큰 값(문자열 JWT)

    @TimeToLive                       // 이 초(seconds)가 지나면 Redis가 이 키를 자동 삭제
    private long ttlSeconds;
}
