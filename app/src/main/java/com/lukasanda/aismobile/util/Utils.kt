/*
 * Copyright 2020 Lukáš Anda. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lukasanda.aismobile.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.RED
import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
import android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE
import android.media.RingtoneManager.TYPE_NOTIFICATION
import android.media.RingtoneManager.getDefaultUri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.SyncCoroutineWorker
import com.lukasanda.aismobile.ui.main.MainActivity
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


const val NOTIFICATION_ID = "ais_notification_id"
const val NOTIFICATION_NAME = "ais"
const val NOTIFICATION_CHANNEL = "ais_channel_sync"
const val NOTIFICATION_WORK = "ais_notification_work"


fun getSessionId(cookies: String): String {
    return cookies.substringBefore(";")
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

operator fun ViewPager2.inc(): ViewPager2 {
    this.setCurrentItem(currentItem + 1, true)
    return this
}

operator fun ViewPager2.dec(): ViewPager2 {
    this.setCurrentItem(currentItem - 1, true)
    return this
}

fun startWorker(applicationContext: Context) {
    val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)

    val request = PeriodicWorkRequest.Builder(
        SyncCoroutineWorker::class.java,
        15,
        TimeUnit.MINUTES
    ).setConstraints(constraints.build())
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(applicationContext)
        .enqueueUniquePeriodicWork(
            "Sync",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
}

fun Response<ResponseBody>.authenticatedOrThrow(): String =
    when {
        this.isSuccessful -> this.body()?.string()
            ?: throw HTTPException()
        this.code() == 403 -> throw AuthException()
        else -> throw HTTPException()
    }

fun sendNotification(applicationContext: Context) {
    val intent = Intent(applicationContext, MainActivity::class.java)
    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK

    val notificationManager =
        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_subjects)
    val titleNotification = applicationContext.getString(R.string.notification_title)
    val subtitleNotification = applicationContext.getString(R.string.notification_subtitle)
    val pendingIntent = getActivity(applicationContext, 0, intent, 0)
    val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
        .setLargeIcon(bitmap).setSmallIcon(R.drawable.ic_subjects)
        .setContentTitle(titleNotification).setContentText(subtitleNotification)
        .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

    notification.priority = PRIORITY_MAX

    if (SDK_INT >= O) {
        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
            .setContentType(CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

        channel.enableLights(true)
        channel.lightColor = RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        channel.setSound(ringtoneManager, audioAttributes)
        notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(12345, notification.build())
}

fun Context.vectorToBitmap(drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: return null
    val bitmap = createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    ) ?: return null
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Int.toARGB(): Int {
    var stringColor = "#" +
            Integer.toHexString(this shr 16 and 0xFF) +
            Integer.toHexString(this shr 8 and 0xFF) +
            Integer.toHexString(this and 0xFF)
    stringColor = stringColor.padEnd(7, 'f')

    return Color.parseColor(stringColor)
}


fun String.getNameFromSender() =
    Pattern.compile("(\\w{2,}+\\.( ){1,})|(, \\w+)").matcher(this).replaceAll("").substringBefore("@").replace(
        ".",
        " "
    ).trim()

fun String.getInitialsFromName() = this.split(" ").map { it.first().toUpperCase() }.joinToString("")

fun getSuggestionRequestString(query: String) =
//    "_suggestKey=${query}&upresneni_default=aktivni_a_preruseni,absolventi,zamestnanci,externiste&_suggestMaxItems=25&vzorek=&_suggestHandler=lide&lang=undefined"
    "_suggestKey=${query}&upresneni_default=aktivni_a_preruseni,zamestnanci&_suggestMaxItems=25&vzorek=&_suggestHandler=lide&lang=undefined"