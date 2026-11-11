# Arkandayim

> **Aras seni izliyor. Öfke barı dolunca gece başlar.**

Arkandayim, Minecraft için yazılmış bir **psikolojik korku** modudur. Klasik "korku modu"
kalıbını tersine çevirir: seni kovalayan şey sürekli peşinde değildir — **görülmeyi bekler**.
Karanlıkta bir şey vardır, ama önce sen ona bakmadan harekete geçmez. Yanlış zamanda doğru
yöne bakmak, ya da karanlıkta sohbete "null" yazmak, onu üzerine salar.

Mod **hâlâ üretim aşamasındadır** (beta). Yeni içerik ve denge ayarları eklenmeye devam
ediyor; aşağıdaki "Sürüm durumu" bölümünü oku.

---

## Oynanabilir Minecraft sürümleri

| Minecraft | Mod yükleyici | Durum |
|-----------|---------------|-------|
| **1.20.1** | **Forge 47.x** (test: 47.4.28) | ✅ **Oynanabilir** — desteklenen tek sürüm |
| 1.20.2 – 1.21.x | Forge / NeoForge | ❌ Desteklenmiyor |
| Fabric / Quilt | — | ❌ Desteklenmiyor |

- **Java:** 17 (Minecraft 1.20.1 ile aynı)
- **Gerekli bağımlılık yok:** sadece Forge. Başka mod kurmana gerek yok.
- **Tek başına veya sunucuda:** tek oyunculu ve çok oyunculu (Forge sunucusu) çalışır.
  Korku efektlerinin bir kısmı istemci tarafındadır, bu yüzden mod hem istemcide hem
  sunucuda kurulu olmalıdır.

---

## Kurulum

1. [Minecraft Forge 47.x](https://files.minecraftforge.net/) ile **1.20.1** profili oluştur.
2. `Arkandayim-1.0.0.jar` dosyasını **Releases** sekmesinden indir.
3. Jar'ı `.minecraft/mods/` klasörüne at.
4. Oyunu Forge profiliyle başlat. Mod listesinde **Arkandayim** görünür.

---

## İçindekiler

### Aras (Null)
Modun merkezindeki varlık. Humanoid, uzun, çenesi açılan, gözleri ve ağzı kendi kendine
parlayan bir figür. **220 can**, 6 zırh, yüksek takip menzili ve geri savrulmaya tamamen
dirençlidir.

- **Gece ortaya çıkar** ve oyuncuyu gözetler; gündüz kaybolur.
- **Ona bakarsan kaybolur** — 1 saniye boyunca nişangahında tutarsan yok olur. Ama öfke
  barın doluysa bu kayboluş bir kaçış değil, arkandan yaklaşmanın başlangıcıdır.
- Öfke barı dolduğunda **yıldırımlar arasında belirir**, oyuncunun kafasını kendine
  çevirir, kısa bir "toparlanma" animasyonundan sonra üzerine koşar. 10 saniye içinde
  yetişirse tek vuruşta öldürür; yetişemezse kaybolur ve bar sıfırlanır.
- **Kahverengi kılıç** ile vurursan ölüm sekansına girer: yıldırım, patlama, kül.
- Kaçarken yapabileceğin tek şey totemlerdir (aşağıya bak).

### Öfke barı (Anger)
Ekranın üstünde duran ince bar. **Sohbete `null` yazmak** barı **+10** doldurur (ve
arkanda bir fısıltı duyarsın). `aras` yazmak hiçbir şey yapmaz. Bar 100'e ulaştığında cam
kırılma sesiyle birlikte av başlar.

### Karanlık sistemi
Mod, "gece = biraz daha karanlık" demez; **ışıksız yer = zifiri karanlık** der.

- Gece gökyüzü ve derin mağaralar neredeyse tamamen kararır; ekran siyah bir perdeyle
  kaplanır ve sadece hafif bir parıltı ile birkaç yıldız kalır.
- **Meşale, fener, lav, ışıldayan blok** gibi ışık kaynakları karanlığı **açar** — etrafına
  ışık koyduğun sürece güvendesin.
- Karanlıkta ekranın kenarlarında **uzaktan bakan gözler** belirir.

### El feneri (H)
- **H** tuşu el fenerini açar/kapatır (tuş atamasından değiştirilebilir).
- Önünde yaklaşık **10 blokluk** bir görüş konisi açar; bu sınırın ötesi artık gri bir sis
  değil, **simsiyah** bir boşluktur — gökyüzü dahil.
- Fener açıkken bile karanlık tamamen gitmez: sadece önünü görürsün.

### Kara bölge (kara orman)
- Dünya **tohumla birlikte** oluşur: her 320 blokluk ızgarada bir hücre, içinde
  **160–250 blok** çapında, kenarları dalgalı bir "kara bölge" olabilir.
- Bölgeye girince arazi siyah toprağa, siyah taşa, kara kütüklere ve kara yapraklara
  dönüşür; ışık seviyesi düşer.
- Bölge senin gelişinle oluşmaz, haritanın bir parçasıdır — aynı tohumda hep aynı yerdedir.

### Eşyalar
| Eşya | Ne yapar |
|------|----------|
| **Kahverengi külçe / cevher** | Modun temel malzemesi |
| **Kahverengi kılıç** | Aras'ı tek vuruşta ölüm sekansına sokar (normal hasar da yüksek) |
| **Kahverengi kazma** | Sağ tık: **cevher nabzı** — çevredeki madenleri sayar ve kırar. Kırdığı madenler **doğrudan envantere** gelir |
| **Kahverengi kürek / balta / çapa** | Kendi özel sağ tık yetenekleri (alan kazma, ağaç devirme, tarla) |
| **Kahverengi zırh seti** | Ateşe dayanıklı, yüksek koruma |
| **Pusula** | Kara kitap evinin yönünü ve mesafesini söyler |
| **Kara kitap** | Modun hikâyesini anlatan okunabilir kitap |
| **Null totemi** | Elinde tutarken Aras sana çarparsa, yıldırımla yok edilir |
| **Gölge totemi** | Sağ tık: seni görünmez yapıp Aras'tan 26 blok uzağa fırlatır |
| **Koruyucu totem** | Sağ tık: kısa süreli kalkan; envanterde dururken **bir ölümcül vuruşu yer** |
| **Arkandayim paneli** | Modun kontrol paneli (aşağıya bak) |

> Not: Bu sürümde **hiçbir tarif (crafting recipe) yoktur**. Eşyalar yaratıcı modda
> **Arkandayim** sekmesinden veya panelden alınır; tarifler ileride eklenecek.

### Panel
Paneli kullanınca animasyonlu bir ekran açılır: 25 eşyanın listesi ve dünya aksiyonları
(av başlatma, ev inşa etme, kara bölge oluşturma, gece/gündüz, öfke avını açma-kapama).
Panelden eşya almak **yaratıcı moda** bağlıdır.

---

## Sürüm durumu — üretim aşamasında

Bu mod **tamamlanmış bir ürün değil, aktif geliştirilen bir beta**dır. Yani:

- Mekanikler oynanabilir ve baştan sona çalışır durumda (yaratık, öfke barı, av sahnesi,
  ölüm sekansı, karanlık, el feneri, aletler, totemler, panel, kara bölge).
- Ama denge değerleri (hasar, süre, bar artışı, karanlık yoğunluğu) hâlâ değişebilir.
- Eksik olanlar: tarifler, doku/model cilası, ses çeşitliliği, İngilizce dil dosyası.
- Kaydedilmiş dünyalar için uyumluluk garantisi yoktur; beta sürümler arasında dünya
  davranışı değişebilir.

Geri bildirim ve hata bildirimi için **Issues** sekmesini kullan.

---

## Kaynaktan derleme

```bash
./gradlew build
```

- Çıktı: `build/libs/arkandayim-1.0.0.jar`
- JDK 17 gerekir.
- Geliştirme için: `./gradlew runClient` veya `./gradlew runServer`

---

## Lisans

**All Rights Reserved.** Kaynak kodu inceleyebilirsin; izinsiz yeniden dağıtım, fork
ederek yayınlama veya başka bir projede kullanma izni verilmez.
