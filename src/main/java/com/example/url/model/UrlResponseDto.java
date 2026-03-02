package com.example.url.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UrlResponseDto {
    private String originalUrl;
    private String shortLink;
    private LocalDateTime expirationDate;
}
