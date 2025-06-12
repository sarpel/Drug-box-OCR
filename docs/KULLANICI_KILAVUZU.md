# 📱 İlaç Kutu OCR Sistemi - Kullanıcı Kılavuzu

## 🎯 **SİSTEM GEREKSİNİMLERİ**

### **Android Cihaz Gereksinimleri**
- **Android Sürümü**: Android 7.0 (API 24) veya üzeri
- **RAM**: Minimum 2GB, önerilen 4GB
- **Depolama**: 500MB boş alan
- **Kamera**: Arka kamera (otofokus önerilen)
- **İnternet**: WiFi veya mobil data bağlantısı
- **Mikrofon**: Sesli komutlar için (opsiyonel)

### **Windows Bilgisayar Gereksinimleri**
- **İşletim Sistemi**: Windows 10 veya üzeri
- **Python**: 3.8 veya üzeri
- **RAM**: Minimum 4GB
- **Ağ**: Android cihazla aynı ağda bağlantı

---

## 🚀 **KURULUM VE İLK KURULUM**

### **1. Android Uygulaması Kurulumu**

#### **APK Kurulumu**
1. APK dosyasını Android cihazınıza indirin
2. **Ayarlar > Güvenlik > Bilinmeyen Kaynaklardan Yükleme** seçeneğini etkinleştirin
3. APK dosyasına dokunarak kurulumu başlatın
4. Kurulum tamamlandığında **"İlaç Kutu OCR"** uygulamasını açın

#### **İlk Açılış Kurulumu**
1. **İzinleri Verin**:
   - Kamera izni (zorunlu)
   - Mikrofon izni (sesli komutlar için)
   - Depolama izni (fotoğraf kaydetme için)
   - Ağ izni (veritabanı senkronizasyonu için)

2. **API Anahtarı Ayarlama**:
   - **Ayarlar** > **API Ayarları**'na gidin
   - Google Gemini API anahtarınızı girin
   - **Kaydet** butonuna basın

3. **Dil Seçimi**:
   - **Ayarlar** > **Genel** > **Dil**
   - **Türkçe** (varsayılan) veya **English** seçin

### **2. Windows İstemcisi Kurulumu**

#### **Python Kurulumu**
```bash
# 1. Python yükleyin (python.org'dan)
# 2. Gerekli paketleri yükleyin:
pip install requests pyautogui keyboard psutil

# 3. İstemci dosyalarını indirin
# windows-client klasörünü masaüstüne kopyalayın
```

#### **Windows İstemcisi Yapılandırması**
1. `config.py` dosyasını düzenleyin:
```python
ANDROID_IP = "192.168.1.100"  # Android cihazın IP adresi
PORT = 8080
PRESCRIPTION_SOFTWARE = "MedPro"  # Kullandığınız reçete yazılımı
```

2. İstemciyi test edin:
```bash
python api_test.py
```

---

## 📖 **TEMEL KULLANIM**

### **Ana Ekran Tanıtımı**

#### **Dashboard Özellikleri**
- **Günlük İstatistikler**: Bugün taranan ilaç sayısı
- **Veritabanı Durumu**: Yüklü ilaç sayısı ve senkronizasyon durumu
- **Windows Bağlantısı**: Bilgisayar bağlantı durumu
- **Hızlı Erişim**: Tarama modları ve ayarlar

#### **Ana Butonlar**
- **📷 Hızlı Tarama**: Tek ilaç tarama
- **📋 Reçete Modu**: Çoklu ilaç reçete oluşturma
- **📑 Şablonlar**: Hazır hastalık şablonları
- **⚙️ Ayarlar**: Uygulama yapılandırması

### **İlaç Tarama İşlemi**

#### **1. Tek İlaç Tarama**
1. **Hızlı Tarama** butonuna basın
2. İlaç kutusunu kamera ile hizalayın:
   - Kutu tam ekranda görünmeli
   - İyi aydınlatma sağlayın
   - Titremeden tutun
3. **Akıllı Mod**'da otomatik yakalama bekleyin veya **Manuel Mod**'da **Yakala** butonuna basın
4. Tanınan metni kontrol edin
5. **Onayla** veya **Düzenle** seçin

#### **2. Sesli Komutlarla Tarama**
1. **🎤 Sesli Komut** butonuna basın
2. Şu komutları kullanabilirsiniz:
   - *"Tarama başlat"*
   - *"Fotoğraf çek"*
   - *"Sonraki ilaç"*
   - *"Reçeteyi tamamla"*
   - *"İptal et"*

#### **3. Barkod Tarama**
1. Kamera ekranında **📷+ ikon** görünce barkodu hizalayın
2. Sistem otomatik olarak barkodu okur
3. Veritabanından ilaç bilgilerini getirir

### **Gelişmiş Eşleştirme Sistemi**

#### **Güven Seviyeleri**
- **🟢 Yüksek (%90+)**: Kesin eşleşme, doğrudan kullanılabilir
- **🟡 Orta (%70-89)**: İyi eşleşme, kontrol önerilen
- **🔴 Düşük (%50-69)**: Belirsiz eşleşme, manuel kontrol gerekli

#### **Eşleştirme Algoritmaları**
1. **Tam Eşleşme**: İlaç adında birebir arama
2. **Benzer Eşleşme**: Harf benzerliği ile arama
3. **Fonetik Eşleşme**: Türkçe sesli benzerlik
4. **Etken Madde**: Aktif bileşen üzerinden arama

---

## 🏥 **REÇETİ YÖNETİMİ**

### **Reçete Oluşturma Süreci**

#### **1. Yeni Reçete Başlatma**
1. **Reçete Modu** butonuna basın
2. **Hasta Bilgileri** (opsiyonel):
   - Hasta TC/ID numarası
   - Hasta adı
3. **Reçete Başlat** butonuna basın

#### **2. İlaç Ekleme**
1. Her ilaç için **İlaç Ekle** butonuna basın
2. İlaç kutusunu tarayın
3. Sonucu doğrulayın:
   - **✅ Onayla**: İlacı reçeteye ekle
   - **✏️ Düzenle**: İsmi manuel düzenle
   - **❌ Reddet**: İlacı ekleme

#### **3. Reçete Önizleme**
- **İlaç Listesi**: Eklenen tüm ilaçları görüntüle
- **Toplam Fiyat**: SGK sonrası hasta payı
- **Süre**: Reçete hazırlanma süresi
- **Durum**: Aktif/Tamamlandı

#### **4. Windows'a Gönderme**
1. **Windows'a Gönder** butonuna basın
2. Sistem otomatik olarak:
   - Windows bilgisayara bağlanır
   - Reçete yazılımını açar
   - İlaçları sırayla yapıştırır
   - F4 tuşuna basarak e-imzaya yönlendirir

### **Reçete Şablonları**

#### **Hazır Şablonlar**
- **🩺 Diyabet**: Metformin, insülin, glukoz ölçer
- **❤️ Hipertansiyon**: ACE inhibitörleri, diüretikler
- **🫁 Solunum**: Bronkodilatatörler, kortikosteroidler
- **🦴 Ağrı**: NSAIDs, analjezikler
- **🧠 Nörolojik**: Antikonvülsanlar, antidepresanlar

#### **Şablon Kullanımı**
1. **Şablonlar** sekmesine gidin
2. Uygun hastalık kategorisini seçin
3. **Şablonu Uygula** butonuna basın
4. Önceden tanımlı ilaçlar reçeteye eklenir
5. Gerekirse manuel ilaç ekleyin/çıkarın

---

## 🔧 **GELİŞMİŞ ÖZELLİKLER**

### **Sesli Komut Sistemi**

#### **Aktif Komutlar**
- **Navigasyon**: "Ana sayfa", "Ayarlar", "Geçmiş"
- **Tarama**: "Tarama başlat", "Fotoğraf çek", "Tekrar çek"
- **Reçete**: "Reçete başlat", "İlaç ekle", "Reçeteyi bitir"
- **Kontrol**: "Onayla", "İptal et", "Yardım"

#### **Sesli Komut Ayarları**
1. **Ayarlar** > **Sesli Komutlar**
2. **Mikrofon Hassasiyeti** ayarlayın
3. **Türkçe Dil Paketi** indirin
4. **Gürültü Filtreleme** etkinleştirin

### **Offline Veritabanı**

#### **Yerel Veritabanı Özellikleri**
- **16,632 Türk İlacı**: Tam liste offline kullanım
- **Etken Madde Bilgileri**: Aktif bileşenler
- **SGK Durumu**: Geri ödeme bilgileri
- **Fiyat Karşılaştırması**: Eşdeğer ilaçlar arası

#### **Veritabanı Güncelleme**
1. **Ayarlar** > **Veritabanı** > **Senkronizasyon**
2. **WiFi bağlantısı** gereklidir
3. **Güncellemeleri Kontrol Et**
4. **Otomatik Güncelleme**: Haftalık

### **Hata Kurtarma Sistemi**

#### **Otomatik Yedekleme**
- **30 saniye aralıklarla** otomatik kaydetme
- **Oturum kurtarma**: Uygulama kapanırsa devam et
- **5 saniye geri alma**: Son işlemi iptal et

#### **Manuel Yedekleme**
1. **Ayarlar** > **Yedekleme**
2. **Yerel Yedek Oluştur**
3. **Yedekleri Dışa Aktar** (CSV/PDF)
4. **Yedekten Geri Yükle**

---

## 📊 **RAPOR VE ANALİTİK**

### **Reçete Geçmişi**

#### **Filtreleme Seçenekleri**
- **Tarih Aralığı**: Bugün, dün, bu hafta, bu ay
- **Hasta Adı**: Belirli hasta kayıtları
- **İlaç Kategorisi**: ATC koduna göre
- **Başarı Durumu**: Tamamlanan/iptal edilen

#### **İstatistik Raporları**
- **Günlük Ortalama**: İlaç sayısı ve süre
- **En Çok Kullanılan**: İlaç ve şablon listesi
- **Başarı Oranı**: Doğru eşleştirme yüzdesi
- **Verimlilik**: Dakika başına ilaç sayısı

### **Dışa Aktarma Seçenekleri**

#### **CSV Formatı**
```csv
Tarih,Hasta ID,İlaç Adı,Etken Madde,Fiyat,SGK Durumu
2024-12-13,12345678901,PARADOLs 500 MG,Parasetamol,15.50,Aktif
```

#### **PDF Raporu**
- **Başlık Sayfası**: Hekim ve hastane bilgileri
- **Reçete Detayları**: Tam ilaç listesi
- **İstatistikler**: Sayısal özetler
- **QR Kod**: Doğrulama için

---

## 🔒 **GÜVENLİK VE UYUMLULUK**

### **Veri Güvenliği**

#### **Yerel Depolama**
- **Şifrelenmiş Veritabanı**: AES-256 şifreleme
- **Güvenli API Anahtarları**: Donanım tabanlı koruma
- **Otomatik Silme**: 90 gün sonra eski kayıtlar

#### **Ağ Güvenliği**
- **HTTPS Bağlantıları**: Tüm API çağrıları şifreli
- **Sertifika Sabitleme**: Man-in-the-middle koruması
- **Yerel Ağ**: Windows bağlantısı sadece yerel ağda

### **KVKK Uyumluluğu**

#### **Kişisel Veri Koruma**
- **Minimal Veri Toplama**: Sadece gerekli bilgiler
- **Kullanıcı Onayı**: Açık rıza alınması
- **Veri Silme Hakkı**: Kullanıcı talep edebilir
- **Veri Taşınabilirliği**: Dışa aktarma imkanı

### **Sağlık Bakanlığı Uyumluluğu**

#### **E-Reçete Entegrasyonu**
- **MEDULA Uyumlu**: Sağlık Bakanlığı standartları
- **E-İmza Desteği**: Elektronik imza entegrasyonu
- **Audit Trail**: Tüm işlemler loglanır
- **Yetkilendirme**: Sadece yetkili personel

---

## 🛠️ **SORUN GİDERME**

### **Sık Karşılaşılan Sorunlar**

#### **Kamera Sorunları**
**Sorun**: Kamera açılmıyor
**Çözüm**: 
1. Kamera iznini kontrol edin
2. Uygulamayı yeniden başlatın
3. Cihazı yeniden başlatın
4. Başka uygulamanın kamerayı kullanmadığından emin olun

**Sorun**: Bulanık görüntü
**Çözüm**:
1. Kamera lensini temizleyin
2. Yeterli ışık sağlayın
3. İlaç kutusunu 15-30 cm mesafeye tutun
4. Otofokus çalışana kadar bekleyin

#### **OCR Sorunları**
**Sorun**: Metin tanınmıyor
**Çözüm**:
1. İlaç kutusunu düz tutun
2. Tüm metin kamera alanında olsun
3. Işığı doğrudan kutuya doğrultun
4. Farklı açılardan deneyin

**Sorun**: Yanlış ilaç eşleşmesi
**Çözüm**:
1. **Gelişmiş Eşleştirme** modunu kullanın
2. Manuel düzenleme yapın
3. Etken madde ile arama yapın
4. Barkod tarama deneyin

#### **Windows Bağlantı Sorunları**
**Sorun**: Windows'a bağlanamıyor
**Çözüm**:
1. Aynı WiFi ağında olduğunuzdan emin olun
2. Windows'da Python istemcisinin çalıştığını kontrol edin
3. Güvenlik duvarı ayarlarını kontrol edin
4. IP adresini manuel olarak ayarlayın

**Sorun**: Otomatik yapıştırma çalışmıyor
**Çözüm**:
1. Reçete yazılımının açık olduğundan emin olun
2. Timing profilini **Muhafazakar** olarak ayarlayın
3. Pencere odaklamasını kontrol edin
4. Klavye kısayollarını test edin

### **Performans Optimizasyonu**

#### **Hız İyileştirmeleri**
1. **Offline Veritabanı**: İnternet gerektirmeden hızlı arama
2. **Önbellek Temizleme**: Ayarlar > Depolama > Önbelleği Temizle
3. **Arka Plan Uygulamaları**: Diğer uygulamaları kapatın
4. **RAM Optimizasyonu**: Cihazı düzenli yeniden başlatın

#### **Pil Tasarrufu**
1. **Brightness**: Ekran parlaklığını azaltın
2. **Kamera Optimizasyonu**: Gereksiz özellikler kapatın
3. **Ağ Kullanımı**: WiFi tercih edin
4. **Arka Plan Senkronizasyonu**: Manuel ayarlayın

---

## 📞 **DESTEK VE İLETİŞİM**

### **Teknik Destek**

#### **Destek Kanalları**
- **Email**: destek@ilackutuocr.com
- **Telefon**: +90 (212) 555-0123
- **WhatsApp**: +90 (532) 555-0123
- **Telegram**: @IlacKutuOCRDestek

#### **Destek Saatleri**
- **Hafta İçi**: 08:00 - 18:00
- **Cumartesi**: 09:00 - 15:00
- **Pazar**: Acil durumlar için WhatsApp

### **Güncelleme ve Sürüm Notları**

#### **Otomatik Güncelleme**
1. **Ayarlar** > **Uygulama** > **Otomatik Güncelleme**
2. WiFi bağlantısında otomatik indir
3. Kritik güvenlik güncellemeleri zorunlu

#### **Sürüm Kontrolü**
- **Mevcut Sürüm**: Ayarlar > Hakkında
- **Güncellemeleri Kontrol Et**: Manuel kontrol
- **Yenilikler**: Güncellemeden sonra bilgilendirme

### **Geri Bildirim ve Öneriler**

#### **Uygulama İçi Geri Bildirim**
1. **Ayarlar** > **Geri Bildirim Gönder**
2. **5 Yıldız Değerlendirme**: Play Store'da
3. **Özellik Önerisi**: E-posta ile
4. **Hata Raporu**: Otomatik log gönderimi

---

## 📈 **İLERİ SEVİYE KULLANIM**

### **API Entegrasyonu**

#### **Özel Veritabanı Bağlantısı**
```python
# Özel hastane veritabanı entegrasyonu
custom_db_config = {
    "host": "hastane-db.example.com",
    "username": "ocr_user",
    "password": "secure_password",
    "database": "hastane_ilac_db"
}
```

#### **Toplu İşlem API'si**
```bash
# Günlük reçete verilerini toplu olarak işleme
curl -X POST "http://android-ip:8080/api/batch/prescriptions" \
  -H "Content-Type: application/json" \
  -d '{"date": "2024-12-13", "export_format": "csv"}'
```

### **Ek Yazılım Entegrasyonları**

#### **Desteklenen Reçete Yazılımları**
- **MedPro**: Tam entegrasyon
- **HospitalIS**: Kısmi entegrasyon
- **DocuMed**: API desteği
- **HealthSoft**: Manuel yapılandırma

#### **BI/Analytics Entegrasyonu**
- **Power BI**: CSV/Excel dışa aktarma
- **Tableau**: JSON API bağlantısı
- **Excel Pivot**: Gelişmiş raporlama
- **Google Sheets**: Real-time sync

---

## ✅ **SONUÇ VE EN İYİ PRATİKLER**

### **Verimli Kullanım İpuçları**

1. **Düzenli Senkronizasyon**: Haftalık veritabanı güncellemesi
2. **Şablon Kullanımı**: Sık reçeteler için şablon oluşturun
3. **Sesli Komutlar**: Hızlı işlem için sesli kontrolü aktif edin
4. **Yedekleme**: Aylık veri yedeklemesi yapın
5. **Performans İzleme**: Başarı oranlarını takip edin

### **Güvenlik Kontrol Listesi**

- [ ] API anahtarları güvenli şekilde saklanıyor
- [ ] Otomatik yedekleme etkin
- [ ] KVKK uyumluluğu sağlanıyor
- [ ] Ağ bağlantıları şifreli
- [ ] Kullanıcı izinleri minimal

### **Kalite Güvence**

- [ ] %95+ tanıma başarı oranı
- [ ] 30 saniye altında reçete tamamlama
- [ ] Sıfır veri kaybı garantisi
- [ ] 7/24 sistem erişilebilirliği
- [ ] Sürekli sistem performans izleme

---

**📱 İlaç Kutu OCR Sistemi - Türk Sağlık Sektörü için Güvenilir Çözüm**

*Son güncelleme: 13 Aralık 2024*
*Sürüm: 4.0.0 - Türkçe Tam Entegrasyon*
