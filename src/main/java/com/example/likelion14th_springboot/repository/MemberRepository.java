package com.example.likelion14th_springboot.repository;
import com.example.likelion14th_springboot.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByEmail(String email);
    Page<Member> findByAgeGreaterThanEqual(Integer age, Pageable pageable);
    List<Member> findByNameStartingWith(String prefix);
    // 이름 중복 검사 쿼리
    boolean existsByName(String name);
    Optional<Member> findByName(String name);
}

