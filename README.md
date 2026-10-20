# ⌨️ KeyWin — Windows & Uzak Masaüstü Donanımsal Sanal Klavyesi

KeyWin, Android cihazlar üzerinden uzak Windows bilgisayarları yönetirken karşılaşılan giriş kısıtlamalarını ortadan kaldırmak için geliştirilmiş özel bir Giriş Yöntemi Düzenleyicisidir (IME).

Standart Android klavyelerinin (Gboard, Samsung Klavye vb.) aksine yazılı metin basmak yerine doğrudan **Donanım Seviyesinde Tuş Olayları (Hardware KeyEvents)** üreterek uzak masaüstü protokollerine fiziki bir USB/Bluetooth mekanik klavye bağlıymış gibi sinyal iletir.

---

## 🌐 Desteklenen ve Uyumlu Platformlar

KeyWin, Android üzerinden uzak bilgisayara bağlandığınız tüm akış ve uzaktan yönetim yazılımlarıyla tam uyumludur:

| Kategori | Desteklenen Yazılımlar | Öne Çıkan Kullanım Senaryosu |
| :--- | :--- | :--- |
| **Oyun Akışı (Game Streaming)** | Moonlight, Sunshine, Parsec | Donanımsal Numpad tuşları, oyun içi Shift/Ctrl koşma kontrolleri |
| **Uzak Masaüstü & Destek** | AnyDesk, TeamViewer, RustDesk | Görev Yöneticisi (`Ctrl+Shift+Esc`), `Alt+Tab`, `Win+D` masaüstü kısayolları |
| **Kurumsal & Sunucu Erişimi** | Microsoft RDP, Chrome Remote Desktop | Güvenli oturum açma, PowerShell komutları, BIOS/Terminal yönetimi |
| **Terminal & Linux** | Termux, VNC Viewer, bVNC | `Ctrl+C` kesme, `Ctrl+Z`, `Tab` otomatik tamamlama ve `F1-F12` tuşları |

---

## ✨ Temel Özellikler

- **Donanım Seviyesinde Tuş İletimi (`sendKeyEvent`):** Basılma (`ACTION_DOWN`) ve bırakılma (`ACTION_UP`) olaylarını ayrı ayrı göndererek Windows'un kısayolları ve basılı tutma hareketlerini sorunsuz tanımasını sağlar.
- **Yapışkan (Sticky / Latch) Modifikatörler:** `Ctrl`, `Alt`, `Win` ve `Shift` tuşlarına basıldığında mavi renkle bekler; ardından gelen tuşla kombinasyonu (Örn: `Alt` + `F4`, `Ctrl` + `V`, `Win` + `R`) Windows'a aktarıp otomatik söner.
- **Dahili Donanımsal Numpad Bloğu:** `[🔢 Numpad / Makro]` sekmesi ile açılan ve oyunların/simülatörlerin ihtiyaç duyduğu bağımsız `KEYCODE_NUMPAD_*` tarama kodlarını üreten tam 4x4 tuş takımı.
- **Tek Dokunuş Windows Makroları:**
  - `TaskMgr`: Tek tıkla `Ctrl + Shift + Esc` (Kilitlenen oyun ve pencereler için kurtarıcı).
  - `Win+D`: Tüm pencereleri küçültüp doğrudan masaüstüne döner.
  - `Alt+Tab`: Açık pencereler arasında hızlı geçiş sağlar.
  - `PrtScn`: Windows ekran alıntısı veya oyun içi görüntü yakalama.
- **📋 Android Panosunu Windows'a Yapıştırma:** Telefonda kopyalanan karmaşık şifreleri, bağlantıları veya Türkçe metinleri tek tıkla Windows'taki imlece aktarır.
- **Dinamik Tuş Görünümleri:** `Shift` basıldığında rakamlar Windows sembollerine (`1` $\rightarrow$ `!`, `4` $\rightarrow$ `$`, `,` $\rightarrow$ `<` vb.) dönüşür; `Caps Lock` veya `Shift` durumuna göre harfler anlık olarak büyür/küçülür.
- **Titreşim ve Görsel Basış Tepkisi:** Dokunulduğunda maviye dönen ve %7 içeri gömülen gerçekçi mekanik basış hissi.
- **Kompakt Boyutlandırma:** Yatay ekran oturumlarında ekranı kapatmamak için tek dokunuşla boyut küçültme ve gizleme çubuğu.

---

## 🚀 Kurulum Adımları

1. [Releases](https://github.com/avare95/keywin/releases) bölümünden en güncel `KeyWin-vX.X.X.apk` dosyasını indirip telefonunuza kurun.
2. Telefonunuzda **Ayarlar > Genel Yönetim (veya Sistem) > Klavye Listesi ve Varsayılan** menüsüne gidin.
3. **KeyWin** anahtarını aktif hale getirin.
4. AnyDesk, Moonlight veya TeamViewer ile uzak oturum başlattığınızda klavye simgesine dokunarak **KeyWin**'i seçin.

> **İpucu:** AnyDesk veya TeamViewer kullanırken uygulamanın ayarlar menüsünden **"Özel tuşları gönder"** (Send special/raw keys) seçeneğinin açık olduğundan emin olun.

---

## 🛠️ Proje Mimarisi

```text
KeyWin/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/keywin/
│   │   │   ├── MainActivity.kt           # Kurulum, test alanı ve GitHub yönlendirmesi
│   │   │   └── WindowsKeyboardService.kt # Klavye donanım mantığı, modifikatörler ve Numpad
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml     # Başlangıç ve tanıtım arayüzü
│   │   │   │   └── keyboard_view.xml     # QWERTY ve Numpad/Makro çift panelli klavye tasarımı
│   │   │   └── values/themes.xml         # Windows OSK renk ve dokunmatik stilleri
│   │   └── AndroidManifest.xml           # IME servis bildirimi
├── LICENSE                               # MIT Lisansı
└── README.md                             # Dokümantasyon