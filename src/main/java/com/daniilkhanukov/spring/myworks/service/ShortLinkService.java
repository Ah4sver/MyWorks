package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.ShortenRequest;
import com.daniilkhanukov.spring.myworks.dto.ShortenResponse;
import com.daniilkhanukov.spring.myworks.entity.ShortLink;
import com.daniilkhanukov.spring.myworks.exception.AliasAlreadyExistsException;
import com.daniilkhanukov.spring.myworks.exception.LinkExpiredException;
import com.daniilkhanukov.spring.myworks.exception.LinkNotFoundException;
import com.daniilkhanukov.spring.myworks.exception.ShortCodeGenerationException;
import com.daniilkhanukov.spring.myworks.repository.ShortLinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class ShortLinkService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final ShortLinkRepository shortLinkRepository;
    private final ShortLinkGenerator shortLinkGenerator;
    private final String baseUrl;
    private final int codeLength;
    private final Clock clock;

    public ShortLinkService(ShortLinkRepository shortLinkRepository, ShortLinkGenerator shortLinkGenerator,
                            @Value("${app.base-url}") String baseUrl,
                            @Value("${app.short-code.length}") int codeLength, Clock clock) {
        this.shortLinkRepository = shortLinkRepository;
        this.shortLinkGenerator = shortLinkGenerator;
        this.baseUrl = baseUrl;
        this.codeLength = codeLength;
        this.clock = clock;
    }


    public ShortenResponse createShortLink(ShortenRequest request) {
        boolean hasAlias = request.alias() != null && !request.alias().isBlank();

        if (hasAlias) {
            return createWithAlias(request);
        }
        return createWithGeneratedCode(request);
    }

    private ShortenResponse createWithAlias(ShortenRequest request) {
        String shortLink = request.alias();

        if (shortLinkRepository.existsByShortLink(shortLink)) {
            throw new AliasAlreadyExistsException(shortLink);
        }

        ShortLink entity = buildShortLink(shortLink, request);

        try {
            ShortLink savedShortLink = shortLinkRepository.saveAndFlush(entity);
            return toResponse(savedShortLink);
        } catch (DataIntegrityViolationException exception) {
            throw new AliasAlreadyExistsException(shortLink);
        }
    }

    public String resolveOriginalUrl(String shortLink) {
        ShortLink link = shortLinkRepository.findByShortLink(shortLink)
                .orElseThrow(() -> new LinkNotFoundException(shortLink));

        if (link.isExpired(clock)) {
            throw new LinkExpiredException(shortLink);
        }

        return link.getLongLink();
    }

    private ShortLink buildShortLink(String shortLink, ShortenRequest request) {
        return ShortLink.builder()
                .shortLink(shortLink)
                .longLink(request.url())
                .alias(request.alias() != null && !request.alias().isBlank())
                .createdAt(OffsetDateTime.now(clock))
                .expiresAt(request.expiresAt())
                .build();
    }

    private ShortenResponse toResponse(ShortLink shortLink) {
        return new ShortenResponse(
                baseUrl + "/" + shortLink.getShortLink(),
                shortLink.getShortLink(),
                shortLink.getLongLink(),
                shortLink.getCreatedAt(),
                shortLink.getExpiresAt()
        );
    }

    private ShortenResponse createWithGeneratedCode(ShortenRequest request) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String shortLink = shortLinkGenerator.generate(codeLength);

            if (shortLinkRepository.existsByShortLink(shortLink)) {
                continue;
            }

            ShortLink entity = buildShortLink(shortLink, request);

            try {
                ShortLink savedShortLink = shortLinkRepository.saveAndFlush(entity);
                return toResponse(savedShortLink);
            } catch (DataIntegrityViolationException exception) {

            }
        }
        throw new ShortCodeGenerationException("Could not generate unique short link after " + MAX_GENERATION_ATTEMPTS + " attempts");
    }

}
