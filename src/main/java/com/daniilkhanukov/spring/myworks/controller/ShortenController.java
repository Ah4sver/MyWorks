package com.daniilkhanukov.spring.myworks.controller;

import com.daniilkhanukov.spring.myworks.dto.ShortenRequest;
import com.daniilkhanukov.spring.myworks.dto.ShortenResponse;
import com.daniilkhanukov.spring.myworks.service.ShortLinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class ShortenController {

    private final ShortLinkService shortLinkService;

    @PostMapping
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest shortenRequest) {
        ShortenResponse response = shortLinkService.createShortLink(shortenRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
