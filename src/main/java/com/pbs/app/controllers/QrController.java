package com.pbs.app.controllers;

import com.pbs.app.services.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrCodeService qrCodeService;

    @GetMapping(value = "/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrForUser(@PathVariable Long userId) {
        byte[] png = qrCodeService.generateQrForUser(userId);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }
}
