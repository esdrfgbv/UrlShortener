package com.example.url.controller;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.url.model.Url;
import com.example.url.model.UrlDto;
import com.example.url.model.UrlErrorResponseDto;
import com.example.url.model.UrlInfoResponseDto;
import com.example.url.model.UrlResponseDto;
import com.example.url.service.UrlService;

import org.apache.commons.lang3.StringUtils;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateShortLink(@RequestBody UrlDto urlDto) {

        if (StringUtils.isBlank(urlDto.getUrl())) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setStatus("400");
            error.setError("URL must not be blank.");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        Url urlToRet = urlService.generateShortUrl(urlDto);

        if (urlToRet != null) {
            UrlResponseDto urlResponseDto = new UrlResponseDto();
            urlResponseDto.setOriginalUrl(urlToRet.getOriginalUrl());
            urlResponseDto.setExpirationDate(urlToRet.getExpirationDate());
            urlResponseDto.setShortLink(urlToRet.getShortLink());
            return new ResponseEntity<>(urlResponseDto, HttpStatus.OK);
        }

        UrlErrorResponseDto error = new UrlErrorResponseDto();
        error.setStatus("500");
        error.setError("There was an error processing your request. Please try again.");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{shortLink:[a-zA-Z0-9]+}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response)
            throws IOException {

        if (StringUtils.isBlank(shortLink)) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setError("Invalid URL.");
            error.setStatus("400");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if (urlToRet == null) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setError("URL does not exist or it might have expired.");
            error.setStatus("404");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        if (urlToRet.getExpirationDate().isBefore(LocalDateTime.now())) {
            urlService.deleteShortlink(urlToRet);
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setError("URL has expired. Please generate a new one.");
            error.setStatus("410");
            return new ResponseEntity<>(error, HttpStatus.GONE);
        }

        response.sendRedirect(urlToRet.getOriginalUrl());
        return ResponseEntity.status(HttpStatus.FOUND).build();
    }

    /**
     * Returns metadata for a short link without performing a redirect.
     * Useful for previewing or inspecting a shortened URL before visiting it.
     */
    @GetMapping("/api/info/{shortLink}")
    public ResponseEntity<?> getUrlInfo(@PathVariable String shortLink) {

        if (StringUtils.isBlank(shortLink)) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setError("Short link must not be blank.");
            error.setStatus("400");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        Url url = urlService.getEncodedUrl(shortLink);

        if (url == null) {
            UrlErrorResponseDto error = new UrlErrorResponseDto();
            error.setError("No URL found for the given short link.");
            error.setStatus("404");
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }

        UrlInfoResponseDto info = new UrlInfoResponseDto();
        info.setShortLink(url.getShortLink());
        info.setOriginalUrl(url.getOriginalUrl());
        info.setCreationDate(url.getCreationDate());
        info.setExpirationDate(url.getExpirationDate());
        info.setExpired(url.getExpirationDate().isBefore(LocalDateTime.now()));

        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
