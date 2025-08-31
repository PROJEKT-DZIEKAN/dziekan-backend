package com.pbs.app.services;

import com.pbs.app.models.User;
import com.pbs.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${python.qr.service.url}")
    private String qrServiceUrl;

    @Value("${spring.application.base-url}")
    private String appBaseUrl;

    public byte[] generateQrForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", user.getId());
        payload.put("first_name", user.getFirstName());
        payload.put("surname", user.getSurname());
        payload.put("base_url", appBaseUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
                qrServiceUrl + "/generate_qr_code", request, byte[].class);

        return response.getBody();
    }

    public Map<Long, byte[]> generateQrAllUsers() {
        Map<Long, byte[]> userQrCodes = new HashMap<>();

        for (User user : userRepository.findAll()) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", user.getId());
            payload.put("first_name", user.getFirstName());
            payload.put("surname", user.getSurname());
            payload.put("base_url", appBaseUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    qrServiceUrl + "/generate_qr_code", request, byte[].class);

            if (response.getBody() != null) {
                userQrCodes.put(user.getId(), response.getBody());
            }
        }

        return userQrCodes;
    }}

