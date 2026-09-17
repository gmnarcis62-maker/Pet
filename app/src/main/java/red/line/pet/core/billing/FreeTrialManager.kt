package red.line.pet.core.billing

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import red.line.pet.core.util.JalaliDateHelper
import java.io.File
import java.security.MessageDigest
import kotlin.math.ceil
import kotlin.math.max

/**
 * Represents the current status of the 30-day Free Trial.
 */
data class TrialInfo(
    val isTrialActive: Boolean,
    val isTrialExpired: Boolean,
    val daysRemaining: Int,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val startDateJalali: String,
    val endDateJalali: String
)

/**
 * Manages the 30-day free trial period with multi-layer tamper-resistant persistence.
 *
 * Persistence layers:
 * 1. Android SharedPreferences (petora_trial_sec_vault)
 * 2. Internal hidden anchor file (.petora_trial_anchor in filesDir)
 * 3. Protected anchor file in noBackupFilesDir (resilient across backups and cleanups)
 * 4. External storage anchor file (if available)
 * 5. Cryptographic signature tied to device hardware ID (ANDROID_ID)
 * 6. Clock-tampering protection (monotonic last-checked timestamp)
 */
class FreeTrialManager(
    private val context: Context
) {
    companion object {
        const val TRIAL_DURATION_DAYS = 30
        const val DAY_IN_MILLIS = 24L * 60L * 60L * 1000L
        const val TRIAL_DURATION_MILLIS = TRIAL_DURATION_DAYS * DAY_IN_MILLIS

        private const val PREFS_NAME = "petora_trial_sec_vault"
        private const val KEY_START_TS = "trial_start_ts"
        private const val KEY_LAST_SEEN_TS = "trial_last_seen_ts"
        private const val KEY_SIGNATURE = "trial_signature"
        private const val ANCHOR_FILE_NAME = ".petora_trial_anchor"
        private const val SALT = "PetoraSecureTrialSalt_2026_V1"
    }

    private val sharedPrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _trialInfo = MutableStateFlow(calculateTrialInfo())
    val trialInfo: StateFlow<TrialInfo> = _trialInfo.asStateFlow()

    init {
        // Initialize and synchronize anchors on creation
        refreshTrialStatus()
    }

    /**
     * Checks if the 30-day free trial is currently active.
     */
    fun isTrialActive(): Boolean {
        refreshTrialStatus()
        return _trialInfo.value.isTrialActive
    }

    /**
     * Checks if the 30-day free trial has expired.
     */
    fun isTrialExpired(): Boolean {
        refreshTrialStatus()
        return _trialInfo.value.isTrialExpired
    }

    /**
     * Gets remaining days in the trial (0 to 30).
     */
    fun getDaysRemaining(): Int {
        refreshTrialStatus()
        return _trialInfo.value.daysRemaining
    }

    /**
     * Recalculates and updates the trial status, checking all security anchors.
     */
    @Synchronized
    fun refreshTrialStatus(): TrialInfo {
        val info = calculateTrialInfo()
        _trialInfo.value = info
        return info
    }

    @SuppressLint("HardwareIds")
    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_device_petora"
        } catch (e: Exception) {
            "unknown_device_petora"
        }
    }

    private fun generateSignature(timestamp: Long): String {
        val raw = "${timestamp}_${getDeviceId()}_$SALT"
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(raw.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            raw.hashCode().toString()
        }
    }

    private fun verifySignature(timestamp: Long, signature: String): Boolean {
        if (timestamp <= 0L || signature.isBlank()) return false
        return generateSignature(timestamp) == signature
    }

    /**
     * Gathers timestamps from all storage anchors and resolves the authoritative start timestamp.
     */
    @Synchronized
    private fun calculateTrialInfo(): TrialInfo {
        val now = System.currentTimeMillis()
        val candidates = mutableListOf<Long>()

        // 1. Check SharedPreferences
        val prefTs = sharedPrefs.getLong(KEY_START_TS, 0L)
        val prefSig = sharedPrefs.getString(KEY_SIGNATURE, "") ?: ""
        if (prefTs > 0L && verifySignature(prefTs, prefSig)) {
            candidates.add(prefTs)
        }

        // 2. Check filesDir anchor
        readAnchorFile(File(context.filesDir, ANCHOR_FILE_NAME))?.let { candidates.add(it) }

        // 3. Check noBackupFilesDir anchor
        try {
            readAnchorFile(File(context.noBackupFilesDir, ANCHOR_FILE_NAME))?.let { candidates.add(it) }
        } catch (ignored: Exception) {}

        // 4. Check externalFilesDir anchor
        try {
            context.getExternalFilesDir(null)?.let { extDir ->
                readAnchorFile(File(extDir, ANCHOR_FILE_NAME))?.let { candidates.add(it) }
            }
        } catch (ignored: Exception) {}

        // Check last known seen timestamp for anti-clock-rollback protection
        val lastSeenTs = sharedPrefs.getLong(KEY_LAST_SEEN_TS, 0L)

        val authoritativeStartTs: Long = if (candidates.isNotEmpty()) {
            // Pick the earliest timestamp across all anchors to prevent bypassing via reinstall/data-clear
            candidates.minOrNull() ?: now
        } else {
            // Brand new installation: record current timestamp
            now
        }

        // Clock rollback detection: if system clock was rolled backwards, advance based on lastSeenTs
        val effectiveNow = if (now < lastSeenTs) {
            lastSeenTs
        } else {
            now
        }

        // Persist and synchronize to all anchors
        persistAnchors(authoritativeStartTs, effectiveNow)

        val elapsed = max(0L, effectiveNow - authoritativeStartTs)
        val isExpired = elapsed >= TRIAL_DURATION_MILLIS
        val isActive = !isExpired

        val remainingMillis = max(0L, TRIAL_DURATION_MILLIS - elapsed)
        val daysRemaining = if (isExpired) {
            0
        } else {
            max(1, ceil(remainingMillis.toDouble() / DAY_IN_MILLIS.toDouble()).toInt()).coerceAtMost(TRIAL_DURATION_DAYS)
        }

        val endTs = authoritativeStartTs + TRIAL_DURATION_MILLIS

        return TrialInfo(
            isTrialActive = isActive,
            isTrialExpired = isExpired,
            daysRemaining = daysRemaining,
            startTimestamp = authoritativeStartTs,
            endTimestamp = endTs,
            startDateJalali = JalaliDateHelper.formatTimestampToJalali(authoritativeStartTs),
            endDateJalali = JalaliDateHelper.formatTimestampToJalali(endTs)
        )
    }

    private fun readAnchorFile(file: File): Long? {
        return try {
            if (file.exists() && file.isFile) {
                val content = file.readText().trim()
                val parts = content.split(":")
                if (parts.size == 2) {
                    val ts = parts[0].toLongOrNull() ?: return null
                    val sig = parts[1]
                    if (verifySignature(ts, sig)) {
                        return ts
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun writeAnchorFile(file: File, timestamp: Long, signature: String) {
        try {
            file.parentFile?.mkdirs()
            file.writeText("$timestamp:$signature")
        } catch (ignored: Exception) {}
    }

    private fun persistAnchors(startTimestamp: Long, currentTimestamp: Long) {
        val signature = generateSignature(startTimestamp)

        // SharedPreferences
        try {
            sharedPrefs.edit()
                .putLong(KEY_START_TS, startTimestamp)
                .putLong(KEY_LAST_SEEN_TS, currentTimestamp)
                .putString(KEY_SIGNATURE, signature)
                .apply()
        } catch (ignored: Exception) {}

        // Internal files
        writeAnchorFile(File(context.filesDir, ANCHOR_FILE_NAME), startTimestamp, signature)

        // noBackup files
        try {
            writeAnchorFile(File(context.noBackupFilesDir, ANCHOR_FILE_NAME), startTimestamp, signature)
        } catch (ignored: Exception) {}

        // External files
        try {
            context.getExternalFilesDir(null)?.let { extDir ->
                writeAnchorFile(File(extDir, ANCHOR_FILE_NAME), startTimestamp, signature)
            }
        } catch (ignored: Exception) {}
    }
}
