# 🛠️ İlaç Kutu OCR - Sorun Giderme Kılavuzu

## 🚨 **ACİL SORUN ÇÖZÜMLERİ**

### **Kritik Sistem Hataları**

#### **Uygulama Açılmıyor**
```
HATA: Uygulama başlatılamadı
ÇÖZÜMLENCİ ADIMLAR:
1. Cihazı yeniden başlat
2. Uygulama önbelleğini temizle: Ayarlar > Uygulamalar > İlaç Kutu OCR > Depolama > Önbelleği Temizle
3. Uygulamayı kaldırıp yeniden yükle
4. Android sürümünü kontrol et (min. Android 7.0)
5. RAM kullanımını kontrol et (min. 2GB boş)
```

#### **Kamera Erişim Hatası**
```
HATA: Camera permission denied / Kamera izni verilmedi
ÇÖZÜMLENCİ ADIMLAR:
1. Ayarlar > Uygulamalar > İlaç Kutu OCR > İzinler > Kamera: ETKİNLEŞTİR
2. Diğer kamera uygulamalarını kapat
3. Cihaz kamerasını test et (Kamera uygulaması ile)
4. Güvenlik duvarı/antivirus uygulamalarını kontrol et
5. Cihaz yöneticisi kısıtlamalarını kontrol et
```

#### **API Bağlantı Hatası**
```
HATA: Failed to connect to OCR service
ÇÖZÜMLENCİ ADIMLAR:
1. İnternet bağlantısını kontrol et
2. API anahtarını doğrula: Ayarlar > API Ayarları
3. Google Gemini API kotasını kontrol et
4. Proxy/VPN ayarlarını kontrol et
5. Sistem tarih/saatini senkronize et
```

---

## 🔧 **DETAYLI SORUN GİDERME**

### **Performans Sorunları**

#### **Yavaş OCR İşlemi**
**BELİRTİLER:**
- 30+ saniye OCR süresi
- Donma/takılma
- Yüksek CPU kullanımı

**TANILAMA:**
```bash
# RAM kullanımını kontrol et
adb shell dumpsys meminfo com.boxocr.simple

# CPU kullanımını kontrol et  
adb shell top -p $(pgrep com.boxocr.simple)

# Ağ latency kontrol
ping 8.8.8.8
```

**ÇÖZÜMLENCİ ADIMLAR:**
1. **Arka plan uygulamalarını kapat:**
   - Ayarlar > Uygulamalar > Çalışan Uygulamalar
   - Gereksiz uygulamaları durdur

2. **Görüntü kalitesini optimize et:**
   - Ayarlar > Kamera > Çözünürlük: Orta (1080p)
   - HDR modunu kapat
   - Arka plan bulanıklığını kapat

3. **Offline moduna geç:**
   - Ayarlar > Veritabanı > Offline Modu: ETKİN
   - İnternet gerektirmeyen yerel arama

4. **Önbellek optimizasyonu:**
   ```
   Ayarlar > Depolama > Önbellek Yönetimi:
   - OCR Önbellek: 100MB
   - Veritabanı Önbellek: 50MB
   - Görüntü Önbellek: 200MB
   ```

#### **Yüksek Pil Tüketimi**
**BELİRTİLER:**
- Saatte %20+ pil tüketimi
- Cihaz ısınması
- Arka plan pil kullanımı

**ÇÖZÜMLENCİ ADIMLAR:**
1. **Pil optimizasyonu:**
   ```
   Ayarlar > Pil > Pil Optimizasyonu:
   - Ekran parlaklığı: %50
   - Kamera flash: Sadece gerektiğinde
   - GPS konum: Kapat
   - Arka plan senkronizasyon: Manuel
   ```

2. **Güç tasarrufu profili:**
   ```
   Ayarlar > Performans > Güç Tasarrufu:
   - CPU frekansı: Dengeli
   - Ağ kullanımı: WiFi öncelikli
   - Görüntü işleme: Düşük güç
   ```

### **OCR Doğruluk Sorunları**

#### **Düşük Tanıma Başarısı**
**BELİRTİLER:**
- %50'den az güven skoru
- Yanlış ilaç eşleştirmeleri
- Eksik/fazla karakterler

**TANILAMA KONTROLÜ:**
```python
# OCR kalite analizi
def analyze_ocr_quality(image_path):
    # Işık seviyesi kontrol
    brightness = calculate_brightness(image)
    
    # Kontrast kontrol  
    contrast = calculate_contrast(image)
    
    # Bulanıklık kontrol
    blur_score = calculate_blur(image)
    
    # Metin yoğunluğu
    text_density = calculate_text_density(image)
    
    return {
        "brightness": brightness,
        "contrast": contrast, 
        "blur": blur_score,
        "text_density": text_density
    }
```

**ÇÖZÜMLENCİ ADIMLAR:**

1. **Fotoğraf kalitesi optimizasyonu:**
   ```
   İDEAL ŞARTLAR:
   - Işık seviyesi: 800-1200 lux
   - Kontrast oranı: 4:1 minimum
   - Bulanıklık skoru: <5%
   - Metin alanı kapsamı: %80+
   ```

2. **Çekim tekniği:**
   - İlaç kutusunu düz zemine koy
   - 90 derece açıdan çek
   - Gölge oluşturmaktan kaçın
   - Tüm metni kamera alanına sığdır
   - 2-3 saniye sabitle

3. **ML model ayarları:**
   ```
   Ayarlar > OCR > Gelişmiş:
   - Model hassasiyeti: Yüksek
   - Türkçe karakter desteği: ETKİN
   - Çok dilli tanıma: ETKİN
   - Gürültü filtreleme: ETKİN
   ```

#### **İlaç Eşleştirme Hataları**
**BELİRTİLER:**
- Doğru OCR, yanlış ilaç
- Eşdeğer ilaç bulunamadı
- Eksik veritabanı bilgileri

**TANILAMA:**
```sql
-- Veritabanı durumu kontrol
SELECT 
    COUNT(*) as total_drugs,
    COUNT(CASE WHEN active = 1 THEN 1 END) as active_drugs,
    COUNT(CASE WHEN sgk_active = 1 THEN 1 END) as sgk_drugs
FROM turkish_drugs;

-- Eşleştirme algoritması test
SELECT drug_name, 
       SIMILARITY('PARAMOL', drug_name) as similarity_score
FROM turkish_drugs 
ORDER BY similarity_score DESC 
LIMIT 10;
```

**ÇÖZÜMLENCİ ADIMLAR:**

1. **Veritabanı güncelleme:**
   ```
   Ayarlar > Veritabanı > Manuel Güncelleme:
   1. WiFi bağlantısını kontrol et
   2. "Veritabanını Güncelle" butonuna bas
   3. İndirme tamamlanana kadar bekle (5-10 dk)
   4. Uygulama yeniden başlat
   ```

2. **Eşleştirme algoritması ayarları:**
   ```
   Ayarlar > Gelişmiş Eşleştirme:
   - Güven eşiği: %70
   - Fonetik eşleştirme: ETKİN
   - Marka-jenerik eşleme: ETKİN
   - Etken madde arama: ETKİN
   ```

3. **Manuel düzeltme:**
   - Eşleştirme ekranında **"Düzenle"** butonunu kullan
   - İlaç adını manuel olarak gir
   - **"Etken Madde ile Ara"** seçeneğini dene
   - Benzer ilaçlar listesinden seç

### **Windows Entegrasyon Sorunları**

#### **Bağlantı Kurulamıyor**
**BELİRTİLER:**
- "Windows bağlantısı başarısız"
- HTTP timeout hataları
- Python istemci başlamıyor

**TANILAMA:**
```bash
# Ağ bağlantısı test
ping [ANDROID_IP]

# Port erişilebilirlik test
telnet [ANDROID_IP] 8080

# Python istemci logları
python windows_automation_client.py --debug

# Güvenlik duvarı kontrol
netsh advfirewall show allprofiles
```

**ÇÖZÜMLENCİ ADIMLAR:**

1. **Ağ yapılandırması:**
   ```bash
   # Windows Command Prompt'ta:
   
   # IP adresini bul
   ipconfig
   
   # Android IP'sini bul (Android'de)
   # Ayarlar > WiFi > Bağlı ağ > Detaylar
   
   # Ping test
   ping [ANDROID_IP]
   ```

2. **Güvenlik duvarı ayarları:**
   ```bash
   # Windows Defender Firewall
   # Gelen Kurallar > Yeni Kural > Port > TCP > 8080
   # İzin Ver > Tüm Profiller
   
   # Giden Kurallar > Yeni Kural > Program 
   # python.exe > İzin Ver
   ```

3. **Python istemci debug:**
   ```python
   # config.py dosyasını kontrol et
   ANDROID_IP = "192.168.1.100"  # Doğru IP
   PORT = 8080                   # Doğru port
   DEBUG = True                  # Debug modunu aç
   
   # Test çalıştır
   python api_test.py
   ```

#### **Otomatik Yapıştırma Çalışmıyor**
**BELİRTİLER:**
- İlaçlar Windows'a geçmiyor
- Yanlış pencereye yapıştırılıyor
- Klavye kısayolları çalışmıyor

**TANILAMA:**
```python
# Pencere tespiti test
import pyautogui
import psutil

# Aktif pencereyi tespit et
active_window = pyautogui.getActiveWindow()
print(f"Aktif pencere: {active_window.title}")

# Reçete yazılımı çalışıyor mu
for proc in psutil.process_iter(['pid', 'name']):
    if 'medpro' in proc.info['name'].lower():
        print(f"Reçete yazılımı bulundu: {proc.info}")
```

**ÇÖZÜMLENCİ ADIMLAR:**

1. **Pencere odaklaması:**
   ```python
   # windows_automation_client.py'de kontrol et:
   
   def focus_prescription_software():
       # Reçete yazılımı penceresini bul
       windows = pyautogui.getAllWindows()
       for window in windows:
           if "MedPro" in window.title:
               window.activate()
               time.sleep(1)
               return True
       return False
   ```

2. **Timing profili ayarlama:**
   ```
   Ayarlar > Windows Entegrasyon > Timing Profili:
   
   MUHAFAZAKAR (önerilen):
   - Pencere odaklama: 2 saniye
   - Yapıştırma arası: 1 saniye  
   - Tuş basma arası: 500ms
   
   STANDART:
   - Pencere odaklama: 1 saniye
   - Yapıştırma arası: 500ms
   - Tuş basma arası: 200ms
   ```

3. **Klavye kısayolu test:**
   ```python
   # Manuel test
   import pyautogui
   
   # Ctrl+V test
   pyautogui.hotkey('ctrl', 'v')
   
   # F4 test  
   pyautogui.press('f4')
   
   # Tab test
   pyautogui.press('tab')
   ```

---

## 📊 **SİSTEM TANI VE İZLEME**

### **Performans Metrikleri**

#### **Başarı Oranı İzleme**
```sql
-- Günlük başarı istatistikleri
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total_scans,
    COUNT(CASE WHEN confidence > 0.9 THEN 1 END) as high_confidence,
    COUNT(CASE WHEN confidence > 0.7 THEN 1 END) as medium_confidence,
    AVG(confidence) as avg_confidence,
    AVG(processing_time_ms) as avg_processing_time
FROM scan_history 
WHERE created_at >= DATE('now', '-7 days')
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

#### **Sistem Kaynak Kullanımı**
```bash
# Android Debug Bridge ile izleme
adb shell dumpsys cpuinfo | grep com.boxocr.simple
adb shell dumpsys meminfo com.boxocr.simple
adb shell dumpsys batterystats | grep com.boxocr.simple
```

### **Otomatik Tanılama Araçları**

#### **Uygulama İçi Tanılama**
```
Ayarlar > Tanılama > Sistem Kontrol:

✅ Kamera erişimi: OK
✅ Mikrofon erişimi: OK  
✅ İnternet bağlantısı: OK
✅ API anahtarı: Geçerli
✅ Veritabanı: 16,632 ilaç yüklü
❌ Windows bağlantısı: Başarısız
✅ Depolama alanı: 2.1GB boş

SORUN TESPİT EDİLDİ:
- Windows istemci çalışmıyor
- Önerilen çözüm: Python istemcisini başlat
```

#### **Log Analizi**
```python
# Log seviyesi ayarlama
Ayarlar > Geliştirici Seçenekleri > Log Seviyesi:

VERBOSE: Tüm detaylar (sadece debug için)
DEBUG: Geliştirici bilgileri  
INFO: Genel bilgiler (önerilen)
WARN: Uyarılar ve hatalar
ERROR: Sadece kritik hatalar

# Log dosyası konumu:
/Android/data/com.boxocr.simple/files/logs/app.log
```

### **Benchmark ve Test Araçları**

#### **OCR Performans Testi**
```python
# Test seti ile otomatik benchmark
def run_ocr_benchmark():
    test_images = [
        "test_samples/paracetamol.jpg",
        "test_samples/aspirin.jpg", 
        "test_samples/amoxicillin.jpg"
    ]
    
    results = []
    for image_path in test_images:
        start_time = time.time()
        ocr_result = perform_ocr(image_path)
        end_time = time.time()
        
        results.append({
            "image": image_path,
            "processing_time": end_time - start_time,
            "confidence": ocr_result.confidence,
            "recognized_text": ocr_result.text
        })
    
    return results

# Benchmark çalıştır:
# Ayarlar > Tanılama > Performans Testi
```

---

## 🔧 **GELİŞMİŞ SORUN GİDERME**

### **Gelişmiş Hata Analizi**

#### **Crash Report Analizi**
```
Hata Kodu: SIGSEGV_11
Zaman: 2024-12-13 14:30:25
Thread: CameraX-core_camera_executor_0

Stack Trace:
at com.boxocr.simple.camera.CameraManager.processImage(CameraManager.kt:245)
at com.boxocr.simple.camera.SmartCameraManager.onImageAvailable(SmartCameraManager.kt:128)

Olası Nedenler:
1. Null pointer exception - resim işleme sırasında
2. Memory leak - yetersiz RAM
3. Camera2 API uyumsuzluğu

Çözümler:
1. Uygulama yeniden başlat
2. Cihaz yeniden başlat  
3. Kamera izinlerini sıfırla
4. Alternatif kamera modu dene
```

#### **Memory Leak Tespiti**
```bash
# Android Studio Memory Profiler ile:
adb shell am dumpheap com.boxocr.simple /data/local/tmp/memory.hprof
adb pull /data/local/tmp/memory.hprof

# Otomatik memory leak tespiti:
Ayarlar > Geliştirici > Memory Monitoring: ETKİN
```

### **Özel Yapılandırma Senaryoları**

#### **Kurumsal WiFi Ayarları**
```
HASTANE AĞI YAPILANDIRMASI:

WiFi Güvenlik: WPA2-Enterprise
EAP Yöntemi: PEAP  
Faz 2 kimlik doğrulama: MSCHAPV2
CA sertifikası: Hastane-CA.crt
Kullanıcı sertifikası: (Belirtme)
Kimlik: [kullanici_adi]@hastane.local
Anonim kimlik: (Boş bırak)
Parola: [domain_parolasi]

Proxy ayarları:
Proxy: Manuel
Proxy sunucusu: proxy.hastane.local
Port: 8080
Baypas etme: localhost,127.0.0.1,*.hastane.local
```

#### **Çoklu Cihaz Senkronizasyonu**
```python
# Merkezi sunucu yapılandırması
CENTRAL_SERVER_CONFIG = {
    "host": "hastane-ocr.internal",
    "port": 443,
    "ssl": True,
    "api_key": "HASTANE_API_KEY",
    "sync_interval": 300,  # 5 dakika
    "devices": [
        {"id": "TABLET_001", "location": "POLIKLINIK_1"},
        {"id": "TABLET_002", "location": "POLIKLINIK_2"},
        {"id": "PHONE_001", "location": "MOBIL_EKIP"}
    ]
}

# Ayarlar > Gelişmiş > Merkezi Sunucu
```

---

## 📞 **DESTEK ESKALASYONu**

### **Seviye 1: Kullanıcı Self-Service**
**Süre**: 0-2 saat
**Araçlar**: 
- Bu kılavuz
- Uygulama içi yardım
- Otomatik tanılama

**Çözülebilir Sorunlar**:
- Temel kullanım soruları
- İzin ve ayar sorunları
- Basit bağlantı sorunları

### **Seviye 2: Teknik Destek**
**Süre**: 2-24 saat
**İletişim**: destek@ilackutuocr.com
**Gerekli Bilgiler**:
```
DESTEK TALEBİ BİLGİLERİ:
- Cihaz modeli ve Android sürümü
- Uygulama sürümü
- Hata mesajı veya ekran görüntüsü
- Sorunun oluşma sıklığı
- Yapılan denemeler
- Log dosyası (varsa)

Örnek:
Cihaz: Samsung Galaxy Tab S8 (Android 13)
Uygulama: v4.0.0
Sorun: OCR %30 başarı oranı
Sıklık: Her kullanımda
Denemeler: Yeniden başlattım, kamerayı temizledim
```

### **Seviye 3: Geliştirici Desteği**  
**Süre**: 1-5 iş günü
**İletişim**: gelistirici@ilackutuocr.com
**Gerekli Durum**:
- Uygulama çökmesi
- Veri kaybı
- Güvenlik sorunları
- API entegrasyon sorunları

### **Seviye 4: Kritik Müdahale**
**Süre**: 1-4 saat
**İletişim**: +90 (532) 555-0123 (WhatsApp)
**Kritik Durumlar**:
- Hastane operasyonlarını etkileyen sorunlar
- Veri güvenliği ihlalleri
- Sistem geneli çökmeler
- Yasal uyumluluk sorunları

---

## 📋 **SORUN GİDERME KONTROL LİSTESİ**

### **Günlük Kontroller**
- [ ] Uygulama başarı oranı >%90
- [ ] Windows bağlantısı aktif
- [ ] Veritabanı güncel (son 7 gün)
- [ ] Depolama alanı >1GB
- [ ] Pil seviyesi >%20

### **Haftalık Kontroller**  
- [ ] Uygulama güncellemesi kontrol
- [ ] Veritabanı tam senkronizasyon
- [ ] Yedekleme kontrolü
- [ ] Performans metrik analizi
- [ ] Hata log review

### **Aylık Kontroller**
- [ ] API kota kullanımı
- [ ] Cihaz performans testi  
- [ ] Güvenlik kontrolü
- [ ] Kullanıcı geri bildirim analizi
- [ ] Sistem kapasitesi planlaması

---

**🛠️ Bu kılavuz sürekli güncellenmektedir. Son sürüm için:** 
**📧 destek@ilackutuocr.com**

*Son güncelleme: 13 Aralık 2024*
*Kılavuz Sürümü: 4.0.0*
