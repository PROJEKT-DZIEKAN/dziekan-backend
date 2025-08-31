package com.pbs.app.controllers;

import com.pbs.app.services.QrCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QrControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private QrCodeService qrCodeService;

    @Test
    void givenUserId_whenGetQrCode_thenReturnsPngImage() {
        Long userId = 1L;
        byte[] expectedQrCodeBytes = "someRealLikeQrCodeBytes".getBytes();


        when(qrCodeService.generateQrForUser(userId)).thenReturn(expectedQrCodeBytes);

        String url = "http://localhost:" + port + "/api/qr/" + userId;
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
        assertThat(response.getBody()).isEqualTo(expectedQrCodeBytes);

        verify(qrCodeService).generateQrForUser(userId);
    }
}