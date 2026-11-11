# Değişiklik günlüğü

Tüm önemli değişiklikler bu dosyada tutulur. Sürüm numaralandırması `major.minor.patch`
biçimindedir ve beta olduğu sürece `1.0.0-betaN` etiketiyle yayınlanır.

## [1.0.0-beta1] — 11.11.2026

İlk halka açık beta sürümü. Mod artık baştan sona oynanabilir: yaratık, öfke barı, av
sahnesi, karanlık sistemi, aletler ve totemler çalışıyor.

### Yeni
- **Aras (Null) varlığı**: 220 can, gece beliren, bakışla tetiklenen av mantığı, yıldırımlı
  "toparlanma" sahnesi ve ölüm sekansı.
- **Öfke barı (Anger)**: sohbete `null` yazmak +10 doldurur, bar dolunca av başlar; `aras`
  etkisizdir.
- **Karanlık sistemi**: gece ve ışıksız mağaralar zifiri karanlık; meşale/fener/lav gibi
  ışık kaynakları karanlığı açar.
- **El feneri (H tuşu)**: yaklaşık 10 blokluk görüş konisi.
- **Kara bölge**: dünyayla birlikte oluşan, 160–250 blok çapında siyah arazi parçaları.
- **Kahverengi ekipman seti**: kılıç, kazma, kürek, balta, çapa ve ateşe dayanıklı zırh.
- **Totemler**: null, gölge ve koruyucu totemi.
- **Panel**: 25 eşyalık liste ve dünya aksiyonları (av, ev, kara bölge, gece/gündüz, av
  anahtarı). Panelden eşya almak yaratıcı moda bağlıdır.
- **Pusula ve kara kitap**, uzaktan bakan gözler, gece ambiyansı ve menüde tehdit atmosferi.

### Bu sürümde eklenen son iki mekanik
- **Kazma madenleri doğrudan envantere veriyor**: kahverengi kazmayla kırılan cevherlerin
  düşüşleri yere düşmeden envantere ekleniyor; blok normal kırılmaya devam ettiği için
  kırılma sesi, alet aşınması ve maden tecrübesi vanilla kalıyor. Envanter doluysa düşüş
  yere bırakılıyor. Kazmanın sağ tık "cevher nabzı" da kırdığı madenleri doğrudan veriyor.
- **El feneri açıkken sis simsiyah**: karanlıkta fener açıldığında görüş sınırının ötesi
  (gökyüzü dahil) siyaha boyanıyor; geçiş yumuşak, fener kapalıyken veya aydınlıkta etkisiz.

### Bilinen eksikler
- Tarif (crafting recipe) yok.
- İngilizce dil dosyası yok.
- Denge değerleri (hasar, süre, karanlık yoğunluğu) beta boyunca değişebilir.
- Beta sürümler arasında dünya uyumluluğu garanti edilmez.
