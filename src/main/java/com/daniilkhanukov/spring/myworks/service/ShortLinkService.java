package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.ShortenRequest;
import com.daniilkhanukov.spring.myworks.dto.ShortenResponse;
import com.daniilkhanukov.spring.myworks.entity.ShortLink;
import com.daniilkhanukov.spring.myworks.exception.AliasAlreadyExistsException;
import com.daniilkhanukov.spring.myworks.exception.LinkExpiredException;
import com.daniilkhanukov.spring.myworks.exception.LinkNotFoundException;
import com.daniilkhanukov.spring.myworks.repository.ShortLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ShortLinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortLinkRepository shortLinkRepository;
    private final ShortLinkGenerator shortLinkGenerator;
    private final String baseUrl;
    private final int codeLength;

    public ShortLinkService(ShortLinkRepository shortLinkRepository, ShortLinkGenerator shortLinkGenerator,
                            @Value("${app.base-url}") String baseUrl,
                            @Value("${app.short-code.length}") int codeLength) {
        this.shortLinkRepository = shortLinkRepository;
        this.shortLinkGenerator = shortLinkGenerator;
        this.baseUrl = baseUrl;
        this.codeLength = codeLength;
    }

    @Transactional
    public ShortenResponse createShortLink(ShortenRequest request) {
        String link;

        if (request.alias() != null && !request.alias().isBlank()) {
            link = request.alias();
            if (shortLinkRepository.existsByShortLink(link)) {
                throw new AliasAlreadyExistsException(link);
            }
        } else {
            link = generateUniqueLink();
        }

        ShortLink shortLink = ShortLink.builder()
                .shortLink(link)
                .longLink(request.url())
                .alias(request.alias() != null && !request.alias().isBlank())
                .createdAt(OffsetDateTime.now())
                .expiresAt(request.expiresAt())
                .build();

        ShortLink savedShortLink = shortLinkRepository.save(shortLink);

        return new ShortenResponse(
                baseUrl + "/" + savedShortLink.getShortLink(),
                savedShortLink.getShortLink(),
                savedShortLink.getLongLink(),
                savedShortLink.getCreatedAt(),
                savedShortLink.getExpiresAt()
        );
    }

    @Transactional
    public String resolveOriginalUrl(String shortLink) {
        ShortLink link = shortLinkRepository.findByShortLink(shortLink)
                .orElseThrow(() -> new LinkNotFoundException(shortLink));

        if (link.isExpired()) {
            throw new LinkExpiredException(shortLink);
        }

        return link.getLongLink();
    }

    private String generateUniqueLink() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String candidate = shortLinkGenerator.generate(codeLength);
            if (!shortLinkRepository.existsByShortLink(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate unique short link after " + MAX_GENERATION_ATTEMPTS + " attempts");

    }











}
