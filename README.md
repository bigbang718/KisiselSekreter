<img width="1080" height="2376" alt="IMG_20261122_012814" src="https://github.com/user-attachments/assets/8869ccc4-0255-487b-83b4-ce3340076395" />
<img width="1080" height="2376" alt="IMG_20261122_012752" src="https://github.com/user-attachments/assets/62cb7c8b-fd18-4ef9-93cc-6da3aab0a0d8" />
<img width="1080" height="2376" alt="IMG_20261122_012730" src="https://github.com/user-attachments/assets/90534797-2b95-4a21-94e6-a4afbfafc86b" />
<img width="1080" height="2376" alt="IMG_20261122_012713" src="https://github.com/user-attachments/assets/55bedb9a-f3f6-4b4b-bcde-594fa6901853" />
<img width="1080" height="2376" alt="IMG_20261122_012657" src="https://github.com/user-attachments/assets/46769764-049c-4e78-9adc-4553723e216d" />
<img width="1080" height="2376" alt="IMG_20261122_012635" src="https://github.com/user-attachments/assets/c8d88cc1-8e62-4403-8469-4a6706668288" />
# Kişisel Sekreter

Sakin, göz yormayan nude renk paletine sahip, odaklanmayı kolaylaştıran ve yargılayıcı olmayan bir yerel Android görev ve rutin takip uygulaması.

Modern üretkenlik uygulamalarındaki karmaşadan, agresif bildirimlerden ve abonelik duvarlarından uzak; kullanıcının hem planlı sorumluluklarını hem de aklına gelen planlanmamış notlarını tek bir huzurlu alanda yönetmesi için tasarlanmıştır.

---

## Öne Çıkan Özellikler

* **Minimal Nude Tasarım & Karanlık Mod:** Sıcak bej, taş ve kil tonları üzerinde yüksek kontrastlı koyu antrasit tipografi. Tek dokunuşla geçiş yapılabilen mat koyu kahve/antrasit tabanlı Karanlık Mod desteği.
* **Özelleştirilebilir Klasör Mimarisi:** Varsayılan olarak gelen 9 ana yaşam alanı klasörü (Ev İşleri, Açıköğretim, Kişisel Bakım, Şifa, Okumalarım [Psikoloji, Mesleki, Edebiyat alt klasörleriyle], Maneviyat, Kur'an Okumalarım, Sosyal İlişkiler, Diğer) ile sınırsız yeni klasör ve alt klasör oluşturma imkânı.
* **Planlı ve Planlanmamış İş Ayrımı:** Tarih baskısı olmadan hızlıca not düşebileceğiniz *Planlanmamış Yapılacaklar* alanı ve tek dokunuşla tarih/hatırlatıcı atayarak *Planlı İşler* listesine dönüştürme akışı.
* **İsteğe Bağlı Sayaç Desteği:** Zikir, okunan sayfa/cüz veya tekrar gerektiren rutinler için görev kartı üzerinden doğrudan artırılıp azaltılabilen (`3/10` gibi) hafif ilerleme sayacı.
* **Esnek Hatırlatıcı ve Sürtünmesiz Erteleme:** Sabit periyotlu (*Rutin*) veya *"tamamlandıktan X gün sonra / günün belirli bir aralığında"* mantığıyla çalışan (*Esnek*) hatırlatmalar. Tek dokunuşla *+2 Saat, Bu Akşam, Yarın, Gelecek Hafta* erteleme seçenekleri ve **Sessiz Saatler** modu.
* **Geçmiş ve Yargılayıcı Olmayan Aylık Rapor:** Tamamlanan işlerin klasör bazlı filtrelenebildiği geçmiş arşivi; tamamlama oranlarını, en istikrarlı olunan alanı ve gözden geçirilmesi gereken ertelenen rutinleri özetleyen aylık analiz ekranı.
* **Ana Ekran Widget'ı (Jetpack Glance):** Uygulamayı açmaya gerek kalmadan doğrudan telefon ana ekranından yaklaşan görevleri görme ve tamamlama imkânı.
* **Tam Çevrimdışı Çalışma ve JSON Yedekleme:** İnternet bağlantısı veya bulut hesabı gerektirmez. Tüm veriler cihaz hafızasında tutulur; tek tıkla JSON olarak dışa aktarılabilir ve yedekten geri yüklenebilir.

---

## Kullanılan Teknolojiler

* **Dil:** Kotlin
* **Kullanıcı Arayüzü:** Jetpack Compose & Material 3
* **Yerel Veritabanı:** Room Persistence Library
* **Arka Plan İşlemleri & Bildirimler:** WorkManager
* **Ana Ekran Widget'ı:** Jetpack Glance
* **Minimum SDK:** Android 7.0 (API 24)

---

## Kurulum ve Çalıştırma

### 1. Hazır APK ile Kurulum
1. Deponun sağ tarafındaki **Releases** bölümünden en güncel `app-debug.apk` dosyasını Android cihazınıza indirin.
2. İndirilen dosyaya dokunun, gerekirse *"Bu kaynaktan yüklemeye izin ver"* seçeneğini onaylayın ve kurulumu tamamlayın.

### 2. Kaynak Koddan Derleme
1. Projeyi **Android Studio** ile açın ve Gradle senkronizasyonunun tamamlanmasını bekleyin.
2. USB hata ayıklaması açık bir Android cihaz bağlayarak veya sanal cihaz (Emulator) üzerinden **Run** butonuna basın.

---

## Veri Gizliliği

**Kişisel Sekreter** hiçbir sunucuya veri göndermez, analitik toplamaz ve hesap oluşturma zorunluluğu sunmaz. Tüm görev, klasör ve istatistik verileri yalnızca kullanıcının kendi cihazındaki yerel Room veritabanında saklanır.

---

## Lisans

Bu proje kişisel kullanım ve açık kaynak paylaşım amacıyla geliştirilmiştir. İhtiyaçlarınıza göre özgürce inceleyebilir, çatallayabilir (fork) ve geliştirebilirsiniz.
