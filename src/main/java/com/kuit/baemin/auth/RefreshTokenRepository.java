package com.kuit.baemin.auth;

import org.springframework.data.repository.CrudRepository;

/**
 * RefreshToken용 Redis 저장소.
 * CrudRepository를 상속하면 save/findById/deleteById 같은 기본 메서드를 Spring Data Redis가 자동 구현해 준다.
 * (JpaRepository가 DB용이듯, 이쪽은 Redis용 — 구현 코드를 우리가 적지 않음)
 *
 * <T, ID> = <저장 객체 타입, @Id 필드 타입> = <RefreshToken, Long(memberId)>
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
