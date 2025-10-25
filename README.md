# ToDoApp (Jetpack Compose)

Basit, hızlı ve modern bir yapılacaklar uygulaması. Jetpack Compose, Room ve AlarmManager kullanır.

## Özellikler
- Görev ekleme/silme/düzenleme
- Başlık ve not için doğrulama (boş içerik engellenir)
- Öncelik ve kategori seçimi
- Dosya ekleri (görsel, pdf, ses, video vb.) ve çoklu silme (uzun basılı tutarak seçim)
- Hatırlatıcı ekleme (yalnızca gelecekteki tarih-saat), bildirim geldiğinde otomatik temizleme
- Sıralama seçenekleri (oluşturulma tarihi, bitiş tarihi, öncelik)
- Tema seçimi (Sistem/Açık/Koyu)

## Kurulum
### Gereksinimler
- Android Studio (Ladybug veya daha güncel)
- JDK 17 (Android Gradle Plugin uyumlu)

### Çalıştırma
- Android Studio: File → Open → proje klasörü → Run
- Komut ile (Windows):
```bash
./gradlew.bat assembleDebug
```
APK, `app/build/intermediates/apk/debug/` altında oluşur.

## Mimari ve Teknolojiler
- UI: Jetpack Compose (Material 3)
- Durum: ViewModel + StateFlow
- Veri: Room (SQLite) + TypeConverters
- Bildirim/Hatırlatıcı: AlarmManager + BroadcastReceiver (`ReminderReceiver`)

## Önemli Dosyalar
- `app/src/main/java/com/abdullah/todoapp/ui/screens/ToDoScreen.kt` (ana liste)
- `app/src/main/java/com/abdullah/todoapp/ui/screens/ToDoDetailScreen.kt` (detay)
- `app/src/main/java/com/abdullah/todoapp/viewmodel/ToDoViewModel.kt`
- `app/src/main/java/com/abdullah/todoapp/model/*` (Room varlık/DAO)
- `app/src/main/java/com/abdullah/todoapp/notification/*` (alarm & bildirim)

## Notlar
- Android 12+ üzerinde exact alarm planlama için cihaz izinleri değişebilir.
- Dosya ekleri `cacheDir` içerisine kopyalanır; kalıcılık gerekiyorsa SAF/MediaStore değerlendirilebilir.

## Katkı
- Fork → branch → PR

## Lisans
- Proje sahibinin tercihine bağlı olarak `LICENSE` eklenebilir.
