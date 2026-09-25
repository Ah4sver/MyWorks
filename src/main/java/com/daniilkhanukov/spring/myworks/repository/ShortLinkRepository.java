package com.daniilkhanukov.spring.myworks.repository;

import com.daniilkhanukov.spring.myworks.entity.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {

    Optional<ShortLink> findByShortLink(String shortLink);

    boolean existsByShortLink(String shortLink);
}
