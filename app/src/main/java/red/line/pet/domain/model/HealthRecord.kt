package red.line.pet.domain.model

enum class HealthRecordType(val titleFa: String) {
    VACCINE("واکسیناسیون"),
    CHECKUP("چکاپ دوره‌ای"),
    MEDICINE("دارو و درمان"),
    SURGERY("جراحی و عقیم‌سازی"),
    DENTAL("بهداشت و دندان"),
    GROOMING("آرایش و اصلاح")
}

data class HealthRecord(
    val id: Long = 0,
    val petId: Long,
    val title: String,
    val type: HealthRecordType,
    val jalaliDate: String,
    val nextDueJalaliDate: String? = null,
    val costToman: Long = 0,
    val clinicOrDoctor: String = "",
    val notes: String = "",
    val isCompleted: Boolean = true
)
