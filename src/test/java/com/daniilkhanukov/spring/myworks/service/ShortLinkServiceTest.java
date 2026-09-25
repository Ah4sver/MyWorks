package com.daniilkhanukov.spring.myworks.service;

import com.daniilkhanukov.spring.myworks.dto.ShortenRequest;
import com.daniilkhanukov.spring.myworks.dto.ShortenResponse;
import com.daniilkhanukov.spring.myworks.entity.ShortLink;
import com.daniilkhanukov.spring.myworks.exception.AliasAlreadyExistsException;
import com.daniilkhanukov.spring.myworks.exception.LinkExpiredException;
import com.daniilkhanukov.spring.myworks.exception.LinkNotFoundException;
import com.daniilkhanukov.spring.myworks.repository.ShortLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortLinkServiceTest {

    @Mock
    private ShortLinkRepository repository;

    @Mock
    private ShortLinkGenerator shortLinkGenerator;

    private ShortLinkService service;

    @BeforeEach
    void setUp() {
        service = new ShortLinkService(repository, shortLinkGenerator, "http://localhost:8080", 7);
    }

    @Test
    void createShortLink_generatesRandomCode_whenAliasNotProvided() {
        ShortenRequest request = new ShortenRequest("https://example.com/very/long/path", null, null);

        when(shortLinkGenerator.generate(7)).thenReturn("abc1234");
        when(repository.existsByShortLink("abc1234")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(invocation -> {
            ShortLink link = invocation.getArgument(0);
            link.setId(1L);
            return link;
        });

        ShortenResponse response = service.createShortLink(request);

        assertThat(response.shortCode()).isEqualTo("abc1234");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/abc1234");
        assertThat(response.originalUrl()).isEqualTo("https://example.com/very/long/path");

        verify(repository).save(any(ShortLink.class));
    }

    @Test
    void createShortLink_usesCustomAlias_whenProvidedAndFree() {
        ShortenRequest request = new ShortenRequest("https://example.com", "my-alias", null);

        when(repository.existsByShortLink("my-alias")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenResponse response = service.createShortLink(request);

        assertThat(response.shortCode()).isEqualTo("my-alias");
        verify(shortLinkGenerator, never()).generate(anyInt());
    }

    @Test
    void createShortLink_throws_whenAliasAlreadyTaken() {
        ShortenRequest request = new ShortenRequest("https://example.com", "taken-alias", null);

        when(repository.existsByShortLink("taken-alias")).thenReturn(true);

        assertThatThrownBy(() -> service.createShortLink(request))
                .isInstanceOf(AliasAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void createShortLink_retriesGeneration_whenCollisionOccurs() {
        ShortenRequest request = new ShortenRequest("https://example.com", null, null);

        when(shortLinkGenerator.generate(7)).thenReturn("dup0001", "dup0001", "free001");
        when(repository.existsByShortLink("dup0001")).thenReturn(true);
        when(repository.existsByShortLink("free001")).thenReturn(false);
        when(repository.save(any(ShortLink.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenResponse response = service.createShortLink(request);

        assertThat(response.shortCode()).isEqualTo("free001");
        verify(shortLinkGenerator, times(3)).generate(7);
    }

    @Test
    void resolveOriginalUrl_returnsUrl() {
        ShortLink link = ShortLink.builder()
                .id(1L)
                .shortLink("abc1234")
                .longLink("https://example.com")
                .createdAt(OffsetDateTime.now())
                .expiresAt(null)
                .build();

        when(repository.findByShortLink("abc1234")).thenReturn(Optional.of(link));

        String url = service.resolveOriginalUrl("abc1234");

        assertThat(url).isEqualTo("https://example.com");
    }

    @Test
    void resolveOriginalUrl_throwsNotFound_whenCodeDoesNotExist() {
        when(repository.findByShortLink("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resolveOriginalUrl("missing"))
                .isInstanceOf(LinkNotFoundException.class);
    }

    @Test
    void resolveOriginalUrl_throwsExpired_whenLinkIsExpired() {
        ShortLink expiredLink = ShortLink.builder()
                .id(1L)
                .shortLink("old0001")
                .longLink("https://example.com")
                .createdAt(OffsetDateTime.now().minusDays(2))
                .expiresAt(OffsetDateTime.now().minusHours(1))
                .build();

        when(repository.findByShortLink("old0001")).thenReturn(Optional.of(expiredLink));

        assertThatThrownBy(() -> service.resolveOriginalUrl("old0001"))
                .isInstanceOf(LinkExpiredException.class);
    }
}
