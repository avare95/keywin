# ⌨️ KeyWin — Moonlight & PC Stream için Windows Sanal Klavyesi

KeyWin, Android cihazlar üzerinden **Moonlight**, **Sunshine** ve uzak masaüstü bağlantılarında Windows işletim sistemini fiziksel bir klavye hassasiyetiyle kontrol etmek için tasarlanmış özel bir Giriş Yöntemi Düzenleyicisidir (IME).

Normal Android klavyelerinin aksine metin çıktısı yerine **donanım düzeyinde tarama kodları (Hardware KeyEvents)** üreterek Windows kısayollarını, oyun kontrollerini ve modifikatör tuş kombinasyonlarını sorunsuz iletir.

---

## ✨ Özellikler

- **Donanım Seviyesinde Tuş İletimi (`sendKeyEvent`):** Moonlight yayın akışının doğrudan yakalayabileceği `ACTION_DOWN` ve `ACTION_UP` sinyalleri iletir.
- **Yapışkan (Latch / Sticky) Modifikatörler:** `Ctrl`, `Alt`, `Win` ve `Shift` tuşlarına basıldığında mavi renkle bekler, ardından basılan tuşla kombinasyonu (Örn: `Alt` + `F4`, `Ctrl` + `C`, `Win` + `R`) Windows'a yollar ve otomatik serbest bırakılır.
- **Dinamik Karakter Dönüşümü:**
    - `Shift` aktifken rakamlar Windows sembollerine (`1` → `!`, `4` → `$`, `,` → `<` vb.) dönüşür.
    - `Caps Lock` veya `Shift` durumuna göre harf etiketleri anlık olarak büyür/küçülür.
- **Dokunsal ve Görsel Geri Bildirim:**
    - Sıfır gecikmeli `OnTouchListener` mimarisi.
    - Basılan tuşun anlık Windows mavisine dönmesi ve %7 oranında içeri çökme animasyonu.
    - Haptic titreşim desteği.
- **Kompakt / Geniş Ekran Modu:** Yatay oyun oturumlarında çalışma alanını kapatmamak için tek dokunuşla boyut küçültme ve klavyeyi kapatma araç çubuğu.
- **Tam Windows Tuş Takımı:** `Esc`, `F1-F12`, `Del`, `Tab`, `Caps`, `Enter`, `Backspace`, `Space` ve `Yön Tuşları`.

---

## 🚀 Kurulum ve Çalıştırma

### 1. APK Olarak Derleme
1. Projeyi Android Studio ile açın.
2. Üst menüden **Build > Build Bundle(s) / APK(s) > Build APK(s)** adımlarını izleyin.
3. Derleme tamamlandığında çıkan bildirimden `locate` seçeneğine tıklayarak `app-debug.apk` dosyasını telefonunuza yükleyin.

### 2. Klavyeyi Android'de Etkinleştirme
1. Telefonunuzda **Ayarlar > Genel Yönetim (veya Sistem) > Klavye Listesi ve Varsayılan** bölümüne gidin.
2. **Windows Klavye (KeyWin)** seçeneğini aktif hale getirin.
3. Bir metin alanına dokunun ve klavye seçim menüsünden **KeyWin**'i seçin.

---

## 🎮 Moonlight Entegrasyon İpuçları

1. **Sistem Tuşlarını Yakalama:** Moonlight Android uygulamasında **Settings > Input Settings > Capture system keys** seçeneğini etkinleştirin. Bu, `Alt+Tab` veya `Win` komutlarının Android tarafından değil doğrudan uzak PC tarafından işlenmesini sağlar.
2. **Klavyeyi Ekrana Getirme:** Moonlight akışı tam ekranken ekrana **3 parmağınızla dokunarak** KeyWin klavyesini doğrudan akış üzerine çağırabilirsiniz.

---

## 🛠️ Proje Mimarisi

```text
KeyWin/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/keywin/
│   │   │   ├── MainActivity.kt           # Klavyeyi etkinleştirme ve test ekranı
│   │   │   └── WindowsKeyboardService.kt # Klavye donanım mantığı ve olay yakalayıcı
│   │   ├── res/
│   │   │   ├── drawable/                 # Vektörel uygulama simgeleri
│   │   │   ├── layout/keyboard_view.xml  # Klavye ızgara ve tuş tasarımı
│   │   │   ├── values/themes.xml         # Windows OSK renk ve stil tanımları
│   │   │   └── xml/method.xml            # Android IME servis tanımı
│   │   └── AndroidManifest.xml           # Servis ve izin konfigürasyonu