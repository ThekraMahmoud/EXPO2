package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class PdfUploadServiceTest {
    private final PdfUploadService service = new PdfUploadService();
    private final byte[] pdf = "%PDF-1.7\nexample".getBytes(StandardCharsets.US_ASCII);

    @Test void acceptsPdfAndGenericBinaryType() {
        assertArrayEquals(pdf, service.readPdf(new MockMultipartFile("file", "menu.PDF", "application/pdf", pdf)));
        assertArrayEquals(pdf, service.readPdf(new MockMultipartFile("file", "menu.pdf", "application/octet-stream", pdf)));
    }

    @Test void rejectsMissingOrEmptyFile() {
        assertThrows(ApiException.class, () -> service.readPdf(null));
        assertThrows(ApiException.class, () -> service.readPdf(new MockMultipartFile("file", new byte[0])));
    }

    @Test void rejectsWrongExtensionTypeAndFakePdf() {
        assertThrows(ApiException.class, () -> service.readPdf(new MockMultipartFile("file", "menu.png", "application/pdf", pdf)));
        assertThrows(ApiException.class, () -> service.readPdf(new MockMultipartFile("file", "menu.pdf", "image/png", pdf)));
        assertThrows(ApiException.class, () -> service.readPdf(new MockMultipartFile("file", "menu.pdf", "application/pdf", "not a PDF".getBytes())));
    }

    @Test void acceptsExactlyFiveMbAndRejectsOneByteMore() {
        byte[] exact = new byte[PdfUploadService.MAX_BYTES];
        System.arraycopy(pdf, 0, exact, 0, pdf.length);
        assertEquals(exact.length, service.readPdf(new MockMultipartFile("file", "a.pdf", "application/pdf", exact)).length);
        assertThrows(ApiException.class, () -> service.readPdf(new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[exact.length + 1])));
    }

    @Test void enforcesActualReadSizeEvenIfReportedSizeIsWrong() {
        var file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[PdfUploadService.MAX_BYTES + 1]) {
            @Override public long getSize() { return 1; }
        };
        assertThrows(ApiException.class, () -> service.readPdf(file));
    }
}
