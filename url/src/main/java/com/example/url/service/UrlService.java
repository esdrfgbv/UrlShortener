package com.example.url.service;

import org.springframework.stereotype.Service;

import com.example.url.model.Url;
import com.example.url.model.UrlDto;

@Service
public interface UrlService {

    public Url generateShortUrl(UrlDto urlDto);

    public Url persistShortlink(Url url);

    public Url getEncodedUrl(String url);

    public void deleteShortlink(Url url);
}
