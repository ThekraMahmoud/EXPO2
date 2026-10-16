package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class PdfUploadService {
    public static final int MAX_BYTES = 5 * 1024 * 1024;

    // نقرأ الملف المرفوع مباشرة؛ لا نحتاج رابطًا أو قائمة مواقع مسموحة.
    public byte[] readPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ApiException("Please upload a non-empty PDF file");
        if (file.getSize() > MAX_BYTES) throw new ApiException("PDF must not exceed 5 MB");
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ApiException("Please upload a .pdf file");
        }
        // بعض الأجهزة ترسل نوعًا عامًا؛ نتحقق من المحتوى نفسه أيضًا ولا نعتمد على الاسم.
        String type = file.getContentType();
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("application/pdf")
                && !type.equalsIgnoreCase("application/octet-stream")) {
            throw new ApiException("Uploaded file must be a PDF");
        }
        try (InputStream input = file.getInputStream()) {
            // قراءة محدودة تحمي الذاكرة حتى لو كان حجم الملف المعلن غير صحيح.
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) throw new ApiException("PDF must not exceed 5 MB");
            if (bytes.length < 5 || !new String(bytes, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new ApiException("Uploaded file does not contain a PDF header");
            }
            return bytes;
        } catch (IOException e) {
            throw new ApiException("Could not read the uploaded PDF; upload it again");
        }
    }
}
