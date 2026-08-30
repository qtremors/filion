package dev.qtremors.filion.ui

import android.content.Context
import android.widget.Toast

fun Context.showFilionToast(
    message: CharSequence,
    longDuration: Boolean = false
) {
    Toast.makeText(
        applicationContext,
        message,
        if (longDuration) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    ).show()
}
