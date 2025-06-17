package com.pbs.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class QrCodeService {

    private final RestTemplate restTemplate;
    private final String qrServiceUrl;

    public QrCodeService(RestTemplateBuilder builder, //deficniuje sobie url z ktorego bedzie pobierac qr kody
                         @Value("${python.qr.service.url}") String qrServiceUrl) {
        this.restTemplate = builder.build();
        this.qrServiceUrl = qrServiceUrl;
    }

    public byte[] generateQrForUser(Long userId) {
        String url = String.format("%s/generate-qr/%d", qrServiceUrl, userId);
        ResponseEntity<byte[]> resp = restTemplate.getForEntity(url, byte[].class);
        return resp.getBody();
    }
}
