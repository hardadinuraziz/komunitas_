# KomunitasKu Lite (Kotlin Android & Web App)

Aplikasi super ringan (**One Day One App MVP**) untuk mengelola:
1. **Checklist Iuran Bulanan Anggota** (1-Tap Lunas / Belum Bayar)
2. **Generator Kartu Tanda Anggota (KTA) Digital** berbasis Native HTML5 Canvas (simpan langsung ke Galeri HP)
3. **Catatan Kas & Generator Laporan Grup WhatsApp Transparan**
4. **Tombol Tagih WA Personal** dengan teks pengingat otomatis

## Struktur Project (Kotlin Ringan + Web Hybrid)

```text
komunitasku-app/
├── index.html                                         # Versi Web Standalone (18 KB, Zero Dependency)
├── settings.gradle.kts                                # Konfigurasi Gradle Kotlin DSL
├── build.gradle.kts                                   # Root build script
├── gradle.properties                                  # Optimasi JVM & AndroidX
└── app/
    ├── build.gradle.kts                               # Konfigurasi APK ultra-ringan (< 2 MB, R8 Minify)
    ├── proguard-rules.pro                             # Aturan ProGuard untuk JavascriptInterface
    └── src/main/
        ├── AndroidManifest.xml                        # Manifest Android
        ├── assets/
        │   └── index.html                             # Bundled Offline Web App untuk Android WebView
        └── java/id/komunitasku/lite/
            └── MainActivity.kt                        # Kotlin Activity + Native Bridge (Galeri, Clipboard & WA Intent)
```

## Keunggulan Arsitektur Ini
- **Ukuran APK < 2 MB:** Tidak memakai library eksternal yang berat, menggunakan pure Android SDK `Activity` + `WebView` + `@JavascriptInterface` di Kotlin.
- **Dual-Platform:** Bisa langsung di-host sebagai Web App (`index.html`) di Vercel/Netlify/GitHub Pages **sekaligus** di-build menjadi APK Android lewat Android Studio.
- **100% Offline-First:** Data tersimpan otomatis di perangkat dan Kartu Anggota langsung tersimpan ke folder `Pictures/KomunitasKu` di Galeri Android.
