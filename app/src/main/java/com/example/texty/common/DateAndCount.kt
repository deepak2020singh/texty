package com.example.texty.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
fun formatTwitterTimestamp(timestamp: Long): String {
    if (timestamp < 0) throw IllegalArgumentException("Timestamp cannot be negative")

    val postTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC)
    val now = ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime()

    if (postTime > now) {
        return DateTimeFormatter.ofPattern("MMM dd yyyy").format(postTime) // Handle future dates
    }

    val seconds = ChronoUnit.SECONDS.between(postTime, now)
    val minutes = ChronoUnit.MINUTES.between(postTime, now)
    val hours = ChronoUnit.HOURS.between(postTime, now)
    val days = ChronoUnit.DAYS.between(postTime, now)
    val months = ChronoUnit.MONTHS.between(postTime, now)

    return when {
        seconds < 10 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 30 -> "${days}d"
        months < 12 -> "${months}mo"
        else -> DateTimeFormatter.ofPattern("MMM dd yyyy").format(postTime)
    }
}

