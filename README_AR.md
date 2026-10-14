# Expo API — نسخة تجهيز الفلوهات

هذا المشروع مبني على هيكلية المشروع الحالية: `Controller / Service / Repository / Model / DTO`، وتمت إضافة الفلوهات الناقصة مع الإبقاء على الملفات الموجودة قدر الإمكان.

## الفلوهات المضافة

1. تسجيل مستخدم وتسجيل دخوله، وتسجيل مطعم ودخول المطعم.
2. انتظار اعتماد المطعم من الإدارة.
3. إدارة القوائم والأصناف، مع التأكد أن الصنف تابع للمطعم المحدد.
4. سلة مستقلة لكل مستخدم ومطعم، وإضافة الأصناف وتعديل الكمية وحذفها.
5. إنشاء طلب مستقل لكل سلة/مطعم، مع حفظ اسم الصنف وسعره وقت الطلب.
6. عرض طلبات العميل، وعرض طلبات المطعم المرتبطة بمعرّف المطعم، وتحديث حالة الطلب.
7. الدفع بالمحفظة: خصم رصيد العميل، إضافة الرصيد لمحفظة المطعم، وإنشاء سجل حركة للطرفين ضمن Transaction واحدة.
8. الدفع الإلكتروني عبر Moyasar: تأكيد الدفع بالرجوع إلى Moyasar والتحقق من الحالة والمبلغ والعملة قبل وضع الطلب كمدفوع وإنشاء الفاتورة.
9. الدفع عند الاستلام/الموقع: يبقى غير مدفوع حتى يؤكد المطعم التحصيل، ثم تُنشأ الفاتورة.
10. تسجيل الأماكن التي زارها المستخدم باستخدام إحداثيات المكان؛ هذا يسجل الزيارة فقط ولا يحل محل مزود خرائط أو حساب المسارات.

## أهم الـ endpoints الجديدة

### Auth

- `POST /api/v1/auth/register/user`
- `POST /api/v1/auth/login/user`
- `POST /api/v1/auth/register/restaurant`
- `POST /api/v1/auth/login/restaurant`

مثال تسجيل مستخدم:
```json
{
  "username": "visitor1",
  "email": "visitor@example.com",
  "phone": "0501234567",
  "password": "ChangeMe123"
}
```

### اعتماد المطاعم (للتجربة فقط إلى أن تضاف صلاحيات JWT)

- `GET /api/v1/admin/restaurants/pending`
- `PATCH /api/v1/admin/restaurants/{sellerId}/verification?status=VERIFIED`

قيم `status`: `VERIFIED` أو `REJECTED` أو `PENDING`.

### القوائم والأصناف

- `GET /api/v1/menus/restaurant/{sellerId}`
- `GET /api/v1/menus/{menuId}/items`
- `POST /api/v1/menus/restaurant/{sellerId}` — body: `{"name":"Main Menu"}`
- `POST /api/v1/menus/{menuId}/restaurant/{sellerId}/items`
- `PUT /api/v1/menus/items/{itemId}/restaurant/{sellerId}`
- `DELETE /api/v1/menus/items/{itemId}/restaurant/{sellerId}` — تعطيل الصنف بدل حذفه للمحافظة على سجل الطلبات.

مثال صنف:
```json
{
  "name": "Chicken Meal",
  "description": "Grilled chicken meal",
  "price": 32.50,
  "available": true
}
```

### السلة

- `GET /api/v1/carts/user/{userId}`
- `POST /api/v1/carts/user/{userId}/restaurant/{sellerId}/items`
- `PUT /api/v1/carts/{cartId}/user/{userId}/items/{itemId}?quantity=2`
- `DELETE /api/v1/carts/{cartId}/user/{userId}/items/{itemId}`
- `DELETE /api/v1/carts/{cartId}/user/{userId}/items`

مثال إضافة صنف:
```json
{"menuItemId": 1, "quantity": 2}
```

### الطلبات والدفع والفاتورة

- `POST /api/v1/orders/user/{userId}/checkout/{cartId}`
- `GET /api/v1/orders/user/{userId}`
- `GET /api/v1/orders/{orderId}/user/{userId}`
- `GET /api/v1/orders/restaurant/{sellerId}`
- `PATCH /api/v1/orders/{orderId}/restaurant/{sellerId}/status`
- `POST /api/v1/orders/{orderId}/user/{userId}/payments/moyasar/{paymentId}/confirm`
- `POST /api/v1/orders/{orderId}/restaurant/{sellerId}/payments/on-site/confirm`

مثال checkout:
```json
{"paymentMethod":"WALLET"}
```

القيم المدعومة: `WALLET`, `MOYASAR`, `VISA`, `APPLE_PAY`, `ON_SITE`. الدفع الإلكتروني يجب أن يتم فعليًا عبر Moyasar من الواجهة ثم إرسال `paymentId` إلى endpoint التأكيد. لا ترسل الواجهة مبلغ الدفع النهائي؛ السيرفر يحسبه من أسعار قاعدة البيانات. طرق `VISA` و`APPLE_PAY` تعتمد على إعداد Moyasar والواجهة الأمامية، وليست تكاملات مستقلة داخل هذا المشروع.

حالات الطلب التي يمكن للمطعم استخدامها: `ACCEPTED`, `PREPARING`, `DELAYED`, `READY`, `COMPLETED`, `CANCELLED`.
حالة الدفع منفصلة: `PENDING` أو `PAID`.

## الأماكن التي زارها المستخدم

الـ endpoint الموجود في المشروع:
- `GET /api/v1/users/{userId}/visits`
- `POST /api/v1/users/{userId}/visits`
- `DELETE /api/v1/users/{userId}/visits/{visitId}`

مثال:
```json
{
  "placeCode": "booth-001",
  "placeName": "Technology Booth",
  "latitude": 24.7136,
  "longitude": 46.6753
}
```

## إعداد التشغيل

1. افتح المشروع في IntelliJ.
2. تأكد من إعداد MySQL في `src/main/resources/application.properties`.
3. احتفظ بنسخة احتياطية من قاعدة البيانات قبل التشغيل؛ تمت إضافة علاقة اختيارية بين الفاتورة والطلب، وكيانات جديدة للقوائم والسلة والطلبات. إذا كانت قاعدة بياناتك القديمة تحتوي على القيد الفريد القديم للسلة، راجع `database/optional_migration.sql` قبل تشغيل الفلو أكثر من مرة لنفس المطعم.
4. عيّن متغير البيئة `MOYASAR_SECRET_KEY` عند اختبار الدفع الإلكتروني. من دون المفتاح، يمكن تشغيل بقية الفلوهات، لكن التحقق الحقيقي من Moyasar لن يعمل.
5. عيّن `OPENROUTER_API_KEY` فقط إذا كنت تستخدم ميزات الذكاء الاصطناعي.
6. شغّل `ExpoApplication` ثم اختبر بالـ Postman حسب التسلسل: تسجيل المطعم → اعتماد المطعم → إنشاء قائمة وصنف → تسجيل المستخدم → إضافة للسلة → checkout → تأكيد الدفع → ظهور الفاتورة.

## ملاحظات مهمة قبل العرض

- **هذه نسخة مناسبة لبدء الاختبار والتجربة، وليست جاهزة للإنتاج الأمني.** تسجيل الدخول يتحقق من كلمة المرور ويرجع هوية المستخدم، لكنه لا يصدر JWT ولا يفرض Spring Security على كل endpoint. لذلك تمرير `userId` أو `sellerId` في الرابط ليس حماية حقيقية؛ لا تعرضها على الإنترنت ولا تستخدم بيانات حقيقية قبل إضافة JWT وتفويض الأدوار والتحقق من هوية صاحب الطلب على مستوى Security.
- endpoint اعتماد المطاعم تحت `/api/v1/admin` غير محمي حتى تتم إضافة JWT وصلاحية Admin.
- فلو `WALLET` يحتاج وجود محفظة مستخدم نشطة ومحفظة مطعم نشطة. لا تتم تعبئة المحفظة تلقائيًا من هذا الفلو.
- الفواتير التي تنشأ من طلبات الطعام مرتبطة بالطلب، ولا تنشأ إلا بعد نجاح الدفع. توجد endpoints قديمة لإدارة الفواتير مباشرة ضمن المشروع الأصلي؛ لا تستخدمها لإنشاء فواتير طلبات الطعام.
- تسجيل الزيارات يحفظ المكان والإحداثيات فقط؛ عرض الخريطة والتوجيه الفعلي يحتاجان ربط الواجهة بمزود خرائط.
- لم أتمكن من تنفيذ `mvn compile/test` في بيئة إعداد هذه النسخة لعدم توفر Maven أو Maven Wrapper في الملفات الأصلية؛ لذلك افتح المشروع في IntelliJ وشغّل Maven compile قبل الاعتماد النهائي، وأرسل أي خطأ يظهر وسأصلحه.
