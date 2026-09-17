package red.line.pet.core.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * Functional Result wrapper for Clean Architecture UseCases and Repositories.
 */
sealed interface AppResult<out T, out E> {
    data class Success<out T>(val data: T) : AppResult<T, Nothing>
    data class Error<out E>(val error: E) : AppResult<Nothing, E>
}

inline fun <T, E, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> {
    return when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> this
    }
}

/**
 * Standard data error categories.
 */
sealed interface DataError {
    enum class Local : DataError {
        DISK_FULL,
        NOT_FOUND,
        VALIDATION_ERROR,
        DATABASE_ERROR,
        VIP_REQUIRED,
        UNKNOWN
    }
}

/**
 * UiText representation allowing either direct Persian strings or Android string resources.
 */
sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(resId, *args)
        }
    }

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resId, *args)
        }
    }
}
