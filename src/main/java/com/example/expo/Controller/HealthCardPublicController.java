package com.example.expo.Controller;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.UserHealthCard;
import com.example.expo.Service.UserHealthCardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.HtmlUtils;
import java.net.URI;

// مساران إضافيان فقط؛ كنترولر الإضافة والتعديل والحذف الحالي لا يتغير.
@RestController
@RequestMapping("/api/v1/health-cards")
public class HealthCardPublicController {
    private final UserHealthCardService service;
    private final String configuredBaseUrl;

    public HealthCardPublicController(UserHealthCardService service,
            @Value("${HEALTH_CARD_PUBLIC_BASE_URL:}") String configuredBaseUrl) {
        this.service = service;
        this.configuredBaseUrl = configuredBaseUrl == null ? "" : configuredBaseUrl.trim();
    }

    @GetMapping(value = "/qr/{userId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showQr(@PathVariable Integer userId) {
        UserHealthCard card = service.getHealthCardForQr(userId);
        String context = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = publicBase(context) + "/api/v1/health-cards/public/" + card.getPublicToken();
        String library = context + "/health-card/qrcode.min.js";
        // الرابط داخل HTML attribute فقط، ولا يُضمّن داخل نص JavaScript قابل للتنفيذ.
        String content = """
                <h1>رمز البطاقة الصحية</h1>
                <p>امسح الرمز لعرض البطاقة. شاركه فقط مع من تسمح له بقراءة بياناتها.</p>
                <div id="qrcode" data-url="%s" aria-label="رمز QR لرابط البطاقة"></div>
                <p><a href="%s">فتح البطاقة الصحية</a></p>
                <script src="%s"></script>
                <script>
                const box = document.getElementById('qrcode');
                if (window.QRCode) {
                  new QRCode(box, {text: box.dataset.url, width: 256, height: 256,
                    correctLevel: QRCode.CorrectLevel.M});
                } else { box.textContent = 'تعذر تحميل QR؛ استخدم رابط البطاقة.'; }
                </script>
                """.formatted(escape(url), escape(url), escape(library));
        return html(page("رمز البطاقة الصحية", content));
    }

    @GetMapping(value = "/public/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showPublicHealthCard(@PathVariable String token) {
        UserHealthCard card = service.getHealthCardByToken(token);
        StringBuilder fields = new StringBuilder();
        field(fields, "فصيلة الدم", card.getBloodType());
        field(fields, "الحساسية", card.getAllergies());
        field(fields, "الأمراض المزمنة", card.getChronicDiseases());
        field(fields, "جهة اتصال الطوارئ", card.getEmergencyContactName());
        field(fields, "هاتف جهة الاتصال", card.getEmergencyContactPhone());
        field(fields, "ملاحظات", card.getNotes());
        String body = "<h1>البطاقة الصحية الرقمية</h1><p>المعلومات مدخلة من صاحب البطاقة.</p>"
                + (fields.isEmpty() ? "<p>لا توجد بيانات طبية مسجلة.</p>" : fields.toString());
        return html(page("البطاقة الصحية", body));
    }

    private String publicBase(String requestBase) {
        if (configuredBaseUrl.isBlank()) return requestBase;
        try {
            URI uri = URI.create(configuredBaseUrl);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null) throw new IllegalArgumentException();
            return configuredBaseUrl.replaceAll("/+$", "");
        } catch (IllegalArgumentException error) {
            throw new ApiException("HEALTH_CARD_PUBLIC_BASE_URL must be a valid http or https base URL");
        }
    }

    private static void field(StringBuilder html, String label, String value) {
        if (value == null || value.isBlank()) return;
        html.append("<section><h2>").append(escape(label)).append("</h2><p>")
                .append(escape(value)).append("</p></section>");
    }
    private static String escape(String value) { return HtmlUtils.htmlEscape(value); }

    private static ResponseEntity<String> html(String body) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .cacheControl(CacheControl.noStore()).header("Referrer-Policy", "no-referrer")
                .header("X-Robots-Tag", "noindex, nofollow").body(body);
    }

    private static String page(String title, String body) {
        return """
                <!doctype html><html lang="ar" dir="rtl"><head><meta charset="UTF-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <meta name="robots" content="noindex,nofollow"><title>%s</title>
                <style>
                *{box-sizing:border-box}body{margin:0;background:#f2f6f5;color:#183d3a;font-family:Tahoma,Arial,sans-serif;line-height:1.8}
                main{max-width:680px;margin:30px auto;padding:24px;background:white;border-radius:16px}
                h1{font-size:25px}h2{font-size:16px;margin-bottom:4px}section{border-bottom:1px solid #dce7e4}
                section p{white-space:pre-wrap;overflow-wrap:anywhere}#qrcode{width:256px;margin:24px auto;direction:ltr}
                a{color:#08695e} @media(max-width:700px){main{margin:12px;padding:18px}}
                </style></head><body><main>%s</main></body></html>
                """.formatted(escape(title), body);
    }
}
