package com.pbs.app.controllers;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.pbs.app.models.User;
import com.pbs.app.services.QrCodeService;
import com.pbs.app.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrCodeService qrCodeService;
    private final UserService userService;

    @GetMapping(value = "/{userId}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrForUser(@PathVariable Long userId) {
        byte[] png = qrCodeService.generateQrForUser(userId);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(png);
    }


    @GetMapping(value = "/allusers", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getQrForAllUsersPdf() {
        Map<Long, byte[]> userQrCodes = qrCodeService.generateQrAllUsers();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            for (Map.Entry<Long, byte[]> entry : userQrCodes.entrySet()) {
                ImageData imageData = ImageDataFactory.create(entry.getValue());
                Image image = new Image(imageData).scaleToFit(200, 200);

                String userName = userService.getUserById(entry.getKey())
                        .map(user -> user.getFirstName() + " " + user.getSurname())
                        .orElse("Unknown User");

                document.add(new Paragraph("Name: " + userName));
                document.add(new Paragraph("USER ID: " + entry.getKey()));
                document.add(image);
                document.add(new Paragraph("\n"));
            }

            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename("all_qr_codes.pdf").build());

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error creating PDF file", e);
        }
    }}

