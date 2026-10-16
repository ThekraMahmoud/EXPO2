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
            @Value("${HEALTH_CARD_PUBLIC_BASE_URL:${app.health-card.public-base-url:}}") String configuredBaseUrl) {
        this.service = service;
        this.configuredBaseUrl = configuredBaseUrl == null ? "" : configuredBaseUrl.trim();
    }

    @GetMapping(value = "/qr/{userId}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> showQr(@PathVariable Integer userId) {
        UserHealthCard card = service.getHealthCardForQr(userId);
        String context = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String url = publicBase(context) + "/api/v1/health-cards/public/" + card.getPublicToken();
        // A relative URL works both directly and through the frontend LAN proxy.
        String library = "/health-card/qrcode.min.js";
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
        return html(page("البطاقة الصحية", body + assistanceForm(token)));
    }

    private static String assistanceForm(String token) {
        String endpoint = "/api/v1/health-cards/public/" + token + "/paramedic-request";
        return """
                <section class="assistance">
                <h2>طلب مسعف لصاحب البطاقة</h2>
                <p>يمكنك طلب المساعدة لصاحب هذه البطاقة دون تسجيل دخول. أدخل بياناتك للتواصل معك وموقع المريض يدويًا.</p>
                <p id="assistance-status" role="status" aria-live="polite"></p>
                <button type="button" id="open-assistance">طلب مسعف لصاحب البطاقة</button>
                <form id="assistance-form" data-endpoint="%s" hidden>
                  <label>اسمك (مقدم البلاغ)<input name="reporterName" autocomplete="name" maxlength="100" required></label>
                  <label>رقم جوالك للتواصل<input name="reporterPhone" type="tel" autocomplete="tel" minlength="7" maxlength="30" dir="ltr" required></label>
                  <label>موقع المريض بالتفصيل<textarea name="locationDescription" maxlength="500" rows="3" placeholder="مثال: جناح السعودية، قرب المدخل، بجوار النافورة" required></textarea></label>
                  <label>ملاحظة للعيادة (اختياري)<textarea name="note" maxlength="1000" rows="2"></textarea></label>
                  <p>سيظهر اسمك ورقم جوالك للعيادة للتواصل معك بشأن طلب المساعدة.</p>
                  <button type="submit" id="send-assistance">إرسال طلب المسعف</button>
                </form>
                <button type="button" id="refresh-assistance" class="secondary">تحديث حالة الطلب</button>
                </section>
                <script>
                (() => {
                  const form = document.getElementById('assistance-form');
                  const status = document.getElementById('assistance-status');
                  const open = document.getElementById('open-assistance');
                  const send = document.getElementById('send-assistance');
                  let sending = false, timer, loading = false;
                  function display(data) {
                    const active = data.status === 'PENDING' || data.status === 'ACCEPTED';
                    open.disabled = active;
                    if (active) form.hidden = true;
                    if (data.status === 'PENDING') status.textContent = 'تم إرسال طلب مسعف لصاحب البطاقة. بانتظار قبول العيادة.';
                    else if (data.status === 'ACCEPTED') status.textContent = 'قبلت العيادة الطلب: ' + (data.clinicName || '');
                    else status.textContent = 'لا يوجد طلب مساعدة مفتوح لصاحب البطاقة حاليًا.';
                  }
                  async function refresh() {
                    if (loading || sending) return;
                    loading = true;
                    try {
                      const response = await fetch(form.dataset.endpoint, {cache: 'no-store', credentials: 'omit'});
                      const data = await response.json();
                      if (!response.ok) throw new Error(data.message || 'تعذر تحديث حالة الطلب.');
                      display(data);
                    } catch (error) { status.textContent = error.message || 'تعذر الاتصال بالعيادة.'; }
                    finally { loading = false; }
                  }
                  open.addEventListener('click', () => {form.hidden = false;form.elements.reporterName.focus();});
                  document.getElementById('refresh-assistance').addEventListener('click', refresh);
                  form.addEventListener('submit', async event => {
                    event.preventDefault();
                    if (sending) return;
                    const body = Object.fromEntries(new FormData(form));
                    for (const key of Object.keys(body)) body[key] = body[key].trim();
                    if (!body.reporterName || !body.reporterPhone || !body.locationDescription) {
                      status.textContent = 'يرجى كتابة اسمك ورقم جوالك وموقع المريض.';return;
                    }
                    sending = true;send.disabled = true;
                    try {
                      const response = await fetch(form.dataset.endpoint, {method:'POST', credentials:'omit', headers:{'Content-Type':'application/json'}, body:JSON.stringify(body)});
                      const data = await response.json();
                      if (!response.ok) throw new Error(data.message || 'تعذر إرسال الطلب.');
                      display(data);
                    } catch (error) { status.textContent = error.message || 'تعذر إرسال الطلب؛ حاولي مرة أخرى.'; }
                    finally {sending = false;send.disabled = false;}
                  });
                  void refresh();
                  timer = setInterval(refresh, 10000);
                  addEventListener('pagehide', () => clearInterval(timer), {once:true});
                })();
                </script>
                """.formatted(escape(endpoint));
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
                .assistance{margin-top:24px;padding-top:16px}.assistance button{padding:12px 18px;border:0;border-radius:12px;background:#6449f8;color:white;cursor:pointer;font:inherit;margin:6px 0}
                .assistance button:disabled{opacity:.55;cursor:default}.assistance .secondary{background:#eeeafa;color:#36267a}.assistance label{display:flex;flex-direction:column;gap:6px;margin:12px 0}
                .assistance input,.assistance textarea{font:inherit;padding:12px;border:1px solid #d5cee6;border-radius:10px;max-width:100%%;width:100%%;box-sizing:border-box}.assistance p{overflow-wrap:anywhere}
                a{color:#08695e} @media(max-width:700px){main{margin:12px;padding:18px}}
                </style></head><body><main>%s</main></body></html>
                """.formatted(escape(title), body);
    }
}

