package com.mazeppa.firebasechat.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Rashad Musayev on 5/2/2023 - 15:19
 */

fun dateFormat(date: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)