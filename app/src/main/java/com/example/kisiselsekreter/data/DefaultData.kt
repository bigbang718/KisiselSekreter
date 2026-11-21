package com.example.kisiselsekreter.data

import com.example.kisiselsekreter.ui.theme.FolderColorTone
import com.example.kisiselsekreter.ui.theme.FolderIconKey

object DefaultData {

    const val FOLDER_EV_ISLERI = "folder_ev_isleri"
    const val FOLDER_ACIKOGRETIM = "folder_acikogretim"
    const val FOLDER_KISISEL_BAKIM = "folder_kisisel_bakim"
    const val FOLDER_SIFA = "folder_sifa"
    const val FOLDER_OKUMALARIM = "folder_okumalarim"
    const val FOLDER_MANEVIYAT = "folder_maneviyat"
    const val FOLDER_KURAN = "folder_kuran"
    const val FOLDER_SOSYAL = "folder_sosyal"
    const val FOLDER_DIGER = "folder_diger"

    const val SUB_OKUMA_PSIKOLOJI = "sub_okuma_psikoloji"
    const val SUB_OKUMA_MESLEKI = "sub_okuma_mesleki"
    const val SUB_OKUMA_EDEBIYAT = "sub_okuma_edebiyat"

    fun defaultFolders(): List<FolderItem> = listOf(
        FolderItem(
            id = FOLDER_EV_ISLERI,
            name = "Ev İşleri",
            colorTone = FolderColorTone.LINEN_SAND,
            iconKey = FolderIconKey.HOME,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_ACIKOGRETIM,
            name = "Açıköğretim",
            colorTone = FolderColorTone.PALE_STONE,
            iconKey = FolderIconKey.SCHOOL,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_KISISEL_BAKIM,
            name = "Kişisel Bakım",
            colorTone = FolderColorTone.POWDER_ROSE,
            iconKey = FolderIconKey.SPA,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_SIFA,
            name = "Şifa",
            colorTone = FolderColorTone.EUCALYPTUS_SAGE,
            iconKey = FolderIconKey.HEALING,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_OKUMALARIM,
            name = "Okumalarım",
            colorTone = FolderColorTone.LATTE_EARTH,
            iconKey = FolderIconKey.BOOK,
            subFolders = listOf(
                SubFolder(id = SUB_OKUMA_PSIKOLOJI, name = "Psikoloji"),
                SubFolder(id = SUB_OKUMA_MESLEKI, name = "Mesleki"),
                SubFolder(id = SUB_OKUMA_EDEBIYAT, name = "Edebiyat")
            ),
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_MANEVIYAT,
            name = "Maneviyat - Dualar ve Zikirler",
            colorTone = FolderColorTone.IVORY_CHAMPAGNE,
            iconKey = FolderIconKey.SPIRITUAL,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_KURAN,
            name = "Kur'an Okumalarım",
            colorTone = FolderColorTone.SOFT_OLIVE,
            iconKey = FolderIconKey.QURAN,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_SOSYAL,
            name = "Sosyal İlişkiler",
            colorTone = FolderColorTone.PEACH_DUST,
            iconKey = FolderIconKey.SOCIAL,
            isDefault = true
        ),
        FolderItem(
            id = FOLDER_DIGER,
            name = "Diğer",
            colorTone = FolderColorTone.WARM_STONE_GREY,
            iconKey = FolderIconKey.FOLDER,
            isDefault = true
        )
    )

    fun defaultSampleTasks(): List<TaskItem> {
        val today = DateTimeUtils.startOfDayMillis(0)
        val tomorrow = DateTimeUtils.startOfDayMillis(1)
        val inTwoDays = DateTimeUtils.startOfDayMillis(2)
        val yesterday = DateTimeUtils.startOfDayMillis(-1)
        val twoDaysAgo = DateTimeUtils.startOfDayMillis(-2)

        return listOf(
            // 1. Ev İşleri
            TaskItem(
                id = "seed_ev_1",
                title = "Haftalık mutfak alışveriş listesini tamamla",
                note = "Zeytinyağı, yulaf, mevsim sebzeleri ve kahve çekirdeği",
                folderId = FOLDER_EV_ISLERI,
                isPlanned = true,
                scheduledDateMillis = today,
                reminderType = ReminderType.FLEXIBLE,
                flexibleRepeatAfterDays = 7,
                flexibleTimeWindow = TimeWindow.EVENING,
                snoozeCount = 2,
                lastSnoozedAtMillis = DateTimeUtils.millisAgo(1, 19, 10),
                createdAtMillis = DateTimeUtils.millisAgo(1, 10)
            ),
            TaskItem(
                id = "seed_ev_2",
                title = "Dolap içi sadeleştirme ve mevsimlik düzenleme",
                note = "Kullanılmayan eşyaları ayırıp bağış kutusuna koy",
                folderId = FOLDER_EV_ISLERI,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(2, 15)
            ),
            TaskItem(
                id = "seed_ev_3",
                title = "Çamaşırları katla ve yerleştir",
                folderId = FOLDER_EV_ISLERI,
                isPlanned = true,
                scheduledDateMillis = yesterday,
                scheduledHour = 20,
                scheduledMinute = 0,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(2, 9),
                completedAtMillis = DateTimeUtils.millisAgo(1, 20, 15)
            ),

            // 2. Açıköğretim
            TaskItem(
                id = "seed_aof_1",
                title = "Vize öncesi ünite özet tekrarları",
                note = "Her ünitenin sonunda kısa kavram kartları çıkar",
                folderId = FOLDER_ACIKOGRETIM,
                isPlanned = true,
                scheduledDateMillis = today,
                scheduledHour = 20,
                scheduledMinute = 0,
                reminderType = ReminderType.ROUTINE,
                routineFrequency = RoutineFrequency.SELECTED_DAYS,
                routineWeekDays = setOf(1, 3, 5),
                hasCounter = true,
                counterCurrent = 2,
                counterTarget = 5,
                counterUnit = "ünite",
                snoozeCount = 3,
                lastSnoozedAtMillis = DateTimeUtils.millisAgo(0, 14, 30),
                createdAtMillis = DateTimeUtils.millisAgo(1, 12)
            ),
            TaskItem(
                id = "seed_aof_2",
                title = "Geçmiş yıl çıkmış sorularını klasörle",
                note = "Çalışma masası için PDF çıktısı alınacaklar listesi",
                folderId = FOLDER_ACIKOGRETIM,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(2, 18)
            ),
            TaskItem(
                id = "seed_aof_3",
                title = "Dönemlik ders çalışma planını oluştur",
                folderId = FOLDER_ACIKOGRETIM,
                isPlanned = true,
                scheduledDateMillis = twoDaysAgo,
                scheduledHour = 16,
                scheduledMinute = 0,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(3, 14),
                completedAtMillis = DateTimeUtils.millisAgo(2, 17, 10)
            ),

            // 3. Kişisel Bakım
            TaskItem(
                id = "seed_bakim_1",
                title = "Akşam cilt bakım rutini ve erken uyku hazırlığı",
                note = "Ekranları 22:00'de bırakıp sakinleştirici müzik aç",
                folderId = FOLDER_KISISEL_BAKIM,
                isPlanned = true,
                scheduledDateMillis = tomorrow,
                reminderType = ReminderType.FLEXIBLE,
                flexibleRepeatAfterDays = 1,
                flexibleTimeWindow = TimeWindow.EVENING,
                createdAtMillis = DateTimeUtils.millisAgo(1, 9)
            ),
            TaskItem(
                id = "seed_bakim_2",
                title = "Temiz içerikli nemlendirici alternatiflerini araştır",
                folderId = FOLDER_KISISEL_BAKIM,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(3, 11)
            ),
            TaskItem(
                id = "seed_bakim_3",
                title = "Sabah 15 dakika esneme ve nefes egzersizi",
                folderId = FOLDER_KISISEL_BAKIM,
                isPlanned = true,
                scheduledDateMillis = today,
                scheduledHour = 8,
                scheduledMinute = 0,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(1, 21),
                completedAtMillis = DateTimeUtils.millisAgo(0, 8, 25)
            ),

            // 4. Şifa
            TaskItem(
                id = "seed_sifa_1",
                title = "Günlük su tüketimi takibi",
                note = "Her saat başı bir bardak ılık su",
                folderId = FOLDER_SIFA,
                isPlanned = true,
                scheduledDateMillis = today,
                reminderType = ReminderType.FLEXIBLE,
                flexibleRepeatAfterDays = 1,
                flexibleTimeWindow = TimeWindow.MORNING,
                hasCounter = true,
                counterCurrent = 5,
                counterTarget = 8,
                counterUnit = "bardak",
                createdAtMillis = DateTimeUtils.millisAgo(0, 7)
            ),
            TaskItem(
                id = "seed_sifa_2",
                title = "Rutin kan tahlili sonuçlarını gözden geçir",
                folderId = FOLDER_SIFA,
                isPlanned = true,
                scheduledDateMillis = tomorrow,
                scheduledHour = 10,
                scheduledMinute = 30,
                createdAtMillis = DateTimeUtils.millisAgo(1, 14)
            ),
            TaskItem(
                id = "seed_sifa_3",
                title = "Akşamları papatya ve melisa çayı karışımı hazırla",
                folderId = FOLDER_SIFA,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(2, 20)
            ),
            TaskItem(
                id = "seed_sifa_4",
                title = "Açık havada 30 dakika sakin yürüyüş",
                folderId = FOLDER_SIFA,
                isPlanned = true,
                scheduledDateMillis = yesterday,
                scheduledHour = 17,
                scheduledMinute = 30,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(2, 11),
                completedAtMillis = DateTimeUtils.millisAgo(1, 18, 5)
            ),

            // 5. Okumalarım (Psikoloji, Mesleki, Edebiyat alt klasörleri)
            TaskItem(
                id = "seed_okuma_1",
                title = "Alışkanlıkların Gücü — günlük okuma",
                note = "Önemli satırların altını çizip özet not çıkar",
                folderId = FOLDER_OKUMALARIM,
                subFolderId = SUB_OKUMA_PSIKOLOJI,
                isPlanned = true,
                scheduledDateMillis = today,
                reminderType = ReminderType.FLEXIBLE,
                flexibleRepeatAfterDays = 2,
                flexibleTimeWindow = TimeWindow.EVENING,
                hasCounter = true,
                counterCurrent = 14,
                counterTarget = 25,
                counterUnit = "sayfa",
                snoozeCount = 2,
                lastSnoozedAtMillis = DateTimeUtils.millisAgo(1, 21, 0),
                createdAtMillis = DateTimeUtils.millisAgo(1, 16)
            ),
            TaskItem(
                id = "seed_okuma_2",
                title = "Güncel mesleki makalelerden not özeti çıkar",
                note = "Hafta sonu sakin bir kahve eşliğinde bakılacak",
                folderId = FOLDER_OKUMALARIM,
                subFolderId = SUB_OKUMA_MESLEKI,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(2, 13)
            ),
            TaskItem(
                id = "seed_okuma_3",
                title = "Kürk Mantolu Madonna — akşam okuması",
                folderId = FOLDER_OKUMALARIM,
                subFolderId = SUB_OKUMA_EDEBIYAT,
                isPlanned = true,
                scheduledDateMillis = tomorrow,
                scheduledHour = 19,
                scheduledMinute = 30,
                hasCounter = true,
                counterCurrent = 10,
                counterTarget = 30,
                counterUnit = "sayfa",
                createdAtMillis = DateTimeUtils.millisAgo(1, 19)
            ),
            TaskItem(
                id = "seed_okuma_4",
                title = "Farkındalık üzerine bölüm notlarını düzenle",
                folderId = FOLDER_OKUMALARIM,
                subFolderId = SUB_OKUMA_PSIKOLOJI,
                isPlanned = true,
                scheduledDateMillis = twoDaysAgo,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(3, 18),
                completedAtMillis = DateTimeUtils.millisAgo(2, 21, 40)
            ),

            // 6. Maneviyat - Dualar ve Zikirler
            TaskItem(
                id = "seed_manev_1",
                title = "Sabah tesbihatı ve şükür zikri",
                folderId = FOLDER_MANEVIYAT,
                isPlanned = true,
                scheduledDateMillis = today,
                scheduledHour = 8,
                scheduledMinute = 30,
                reminderType = ReminderType.ROUTINE,
                routineFrequency = RoutineFrequency.DAILY,
                hasCounter = true,
                counterCurrent = 21,
                counterTarget = 33,
                createdAtMillis = DateTimeUtils.millisAgo(0, 7, 30)
            ),
            TaskItem(
                id = "seed_manev_2",
                title = "Akşam salavat ve istiğfar",
                folderId = FOLDER_MANEVIYAT,
                isPlanned = true,
                scheduledDateMillis = tomorrow,
                scheduledHour = 21,
                scheduledMinute = 30,
                reminderType = ReminderType.ROUTINE,
                routineFrequency = RoutineFrequency.DAILY,
                hasCounter = true,
                counterCurrent = 40,
                counterTarget = 100,
                createdAtMillis = DateTimeUtils.millisAgo(0, 9)
            ),
            TaskItem(
                id = "seed_manev_3",
                title = "Sevdiğim duaları tek bir defterde topla",
                folderId = FOLDER_MANEVIYAT,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(4, 15)
            ),
            TaskItem(
                id = "seed_manev_4",
                title = "Sabah duaları",
                folderId = FOLDER_MANEVIYAT,
                isPlanned = true,
                scheduledDateMillis = today,
                scheduledHour = 7,
                scheduledMinute = 15,
                hasCounter = true,
                counterCurrent = 33,
                counterTarget = 33,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(1, 22),
                completedAtMillis = DateTimeUtils.millisAgo(0, 7, 45)
            ),

            // 7. Kur'an Okumalarım
            TaskItem(
                id = "seed_kuran_1",
                title = "Günlük cüz ve meal okuması",
                note = "4. Cüz — sakin ve anlayarak okuma",
                folderId = FOLDER_KURAN,
                isPlanned = true,
                scheduledDateMillis = today,
                scheduledHour = 7,
                scheduledMinute = 30,
                reminderType = ReminderType.ROUTINE,
                routineFrequency = RoutineFrequency.DAILY,
                hasCounter = true,
                counterCurrent = 6,
                counterTarget = 20,
                counterUnit = "sayfa",
                createdAtMillis = DateTimeUtils.millisAgo(0, 6, 45)
            ),
            TaskItem(
                id = "seed_kuran_2",
                title = "Kehf Suresi tefsir notlarını incele",
                folderId = FOLDER_KURAN,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(3, 10)
            ),
            TaskItem(
                id = "seed_kuran_3",
                title = "3. Cüz okuması",
                folderId = FOLDER_KURAN,
                isPlanned = true,
                scheduledDateMillis = yesterday,
                scheduledHour = 8,
                scheduledMinute = 0,
                hasCounter = true,
                counterCurrent = 20,
                counterTarget = 20,
                counterUnit = "sayfa",
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(2, 7),
                completedAtMillis = DateTimeUtils.millisAgo(1, 9, 20)
            ),
            TaskItem(
                id = "seed_kuran_4",
                title = "2. Cüz okuması",
                folderId = FOLDER_KURAN,
                isPlanned = true,
                scheduledDateMillis = DateTimeUtils.startOfDayMillis(-3),
                scheduledHour = 8,
                scheduledMinute = 0,
                hasCounter = true,
                counterCurrent = 20,
                counterTarget = 20,
                counterUnit = "sayfa",
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(4, 7),
                completedAtMillis = DateTimeUtils.millisAgo(3, 8, 50)
            ),
            TaskItem(
                id = "seed_kuran_5",
                title = "1. Cüz okuması",
                folderId = FOLDER_KURAN,
                isPlanned = true,
                scheduledDateMillis = DateTimeUtils.startOfDayMillis(-5),
                scheduledHour = 7,
                scheduledMinute = 45,
                hasCounter = true,
                counterCurrent = 20,
                counterTarget = 20,
                counterUnit = "sayfa",
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(6, 7),
                completedAtMillis = DateTimeUtils.millisAgo(5, 8, 35)
            ),

            // 8. Sosyal İlişkiler
            TaskItem(
                id = "seed_sosyal_1",
                title = "Aileyi ara ve hafta sonu kahvaltı planını konuş",
                folderId = FOLDER_SOSYAL,
                isPlanned = true,
                scheduledDateMillis = tomorrow,
                reminderType = ReminderType.FLEXIBLE,
                flexibleRepeatAfterDays = 7,
                flexibleTimeWindow = TimeWindow.AFTERNOON,
                createdAtMillis = DateTimeUtils.millisAgo(1, 13)
            ),
            TaskItem(
                id = "seed_sosyal_2",
                title = "Uzun zamandır görüşmediğim arkadaşıma hal hatır mesajı at",
                folderId = FOLDER_SOSYAL,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(2, 19)
            ),
            TaskItem(
                id = "seed_sosyal_3",
                title = "Doğum günü tebriği gönder",
                folderId = FOLDER_SOSYAL,
                isPlanned = true,
                scheduledDateMillis = twoDaysAgo,
                isCompleted = true,
                createdAtMillis = DateTimeUtils.millisAgo(3, 12),
                completedAtMillis = DateTimeUtils.millisAgo(2, 11, 5)
            ),

            // 9. Diğer
            TaskItem(
                id = "seed_diger_1",
                title = "Aylık fatura ve dijital abonelik kontrolü",
                note = "Kullanılmayan abonelikleri iptal et",
                folderId = FOLDER_DIGER,
                isPlanned = true,
                scheduledDateMillis = inTwoDays,
                scheduledHour = 11,
                scheduledMinute = 0,
                reminderType = ReminderType.ROUTINE,
                routineFrequency = RoutineFrequency.MONTHLY,
                createdAtMillis = DateTimeUtils.millisAgo(1, 15)
            ),
            TaskItem(
                id = "seed_diger_2",
                title = "Telefon galerisindeki eski ekran görüntülerini temizle",
                folderId = FOLDER_DIGER,
                isPlanned = false,
                createdAtMillis = DateTimeUtils.millisAgo(4, 17)
            )
        )
    }
}
