package com.pbs.app.controllers;

import com.pbs.app.services.QrCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyLong;

@WebMvcTest(QrController.class)
class QrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrCodeService qrCodeService;

    @Test
    void givenUserId_whenGetQrCode_thenReturnsPngImage() throws Exception {
        Long userId = 1L;
        byte[] mockQrCodeBytes = "mockQrCodeContentForTest".getBytes();

        when(qrCodeService.generateQrForUser(userId)).thenReturn(mockQrCodeBytes);

        mockMvc.perform(get("/api/qr/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(mockQrCodeBytes));

        verify(qrCodeService).generateQrForUser(userId);
    }
}