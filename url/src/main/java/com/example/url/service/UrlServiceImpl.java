package com.example.url.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.url.model.Url;
import com.example.url.model.UrlDto;
import com.example.url.repository.UrlRepository;
import com.google.common.hash.Hashing;

import org.apache.commons.lang3.StringUtils;

@Component
public class UrlServiceImpl implements UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Override
    public Url generateShortUrl(UrlDto urlDto) {
        if (StringUtils.isNotEmpty(urlDto.getUrl())) {
            String encodedUrl = encodeUrl(urlDto.getUrl());
            Url urlToPersist = new Url();
            urlToPersist.setCreationDate(LocalDateTime.now());
            urlToPersist.setOriginalUrl(urlDto.getUrl());
            urlToPersist.setShortLink(encodedUrl);
            urlToPersist
                    .setExpirationDate(getExpirationDate(urlDto.getExpirationDate(), urlToPersist.getCreationDate()));
            Url urlToRet = persistShortlink(urlToPersist);

            if (urlToRet != null)
                return urlToRet;

            return null;
        }
        return null;
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if (StringUtils.isBlank(expirationDate)) {
            return creationDate.plusSeconds(60);
        }
        LocalDateTime expirationDateToRet = LocalDateTime.parse(expirationDate);
        return expirationDateToRet;
    }

    private String encodeUrl(String url) {
        String encodedUrl = "";
        LocalDateTime time = LocalDateTime.now();
        encodedUrl = Hashing.murmur3_32()
                .hashString(url.concat(time.toString()), StandardCharsets.UTF_8)
                .toString();
        return encodedUrl;
    }

    @Override
    public Url persistShortlink(Url url) {
        Url urlToRet = urlRepository.save(url);
        return urlToRet;
    }

    @Override
    public Url getEncodedUrl(String url) {
        Url urlToRet = urlRepository.findByShortLink(url);
        return urlToRet;
    }

    @Override
    public void deleteShortlink(Url url) {
        urlRepository.delete(url);
    }

}
