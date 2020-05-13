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
import android.content.res.Resources
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import androidx.work.*
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lukasanda.aismobile.R
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.RescheduleWorker
import com.lukasanda.aismobile.data.remote.SyncCoroutineWorker
import com.lukasanda.aismobile.ui.main.MainActivity
import com.snakydesign.livedataextensions.nonNull
import kotlinx.coroutines.delay
import okhttp3.ResponseBody
import retrofit2.Response
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


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

fun startSingleWorker(applicationContext: Context) {
    val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)

    val request = OneTimeWorkRequest.Builder(SyncCoroutineWorker::class.java).setConstraints(constraints.build()).addTag("Sync").build()
    val request2 = OneTimeWorkRequest.Builder(RescheduleWorker::class.java).setConstraints(constraints.build()).addTag("Reschedule").build()
    WorkManager.getInstance(applicationContext).beginUniqueWork("Sync", ExistingWorkPolicy.REPLACE, request).then(request2).enqueue()
}

fun startSingleWorkerWithDelay(applicationContext: Context) {
    val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)

    val request = OneTimeWorkRequest.Builder(
        SyncCoroutineWorker::class.java
    ).setConstraints(constraints.build())
        .addTag("Sync")
        .setInitialDelay(5, TimeUnit.MINUTES)
//        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(applicationContext)
        .enqueueUniqueWork(
            "Sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
}

fun startWorker(applicationContext: Context) {
    val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)

    val request = PeriodicWorkRequest.Builder(
        SyncCoroutineWorker::class.java,
        15,
        TimeUnit.MINUTES
    ).setConstraints(constraints.build())
        .addTag("Sync2")
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

fun Response<String>.authenticatedOrThrow2(): String =
    when {
        this.isSuccessful -> this.body() ?: throw HTTPException()
        this.code() == 403 -> throw AuthException()
        else -> throw HTTPException()
    }

suspend fun Response<ResponseBody>.authenticatedOrReturn(func: suspend (String) -> ResponseResult): ResponseResult {
    return when {
        this.isSuccessful -> this.body()?.string()?.let {
            func(it)
        } ?: ResponseResult.NetworkError
        this.code() == 403 -> ResponseResult.AuthError
        else -> ResponseResult.NetworkError
    }
}

suspend fun Response<String>.authenticatedOrReturn2(func: suspend (String) -> ResponseResult): ResponseResult {
    return when {
        this.isSuccessful -> this.body()?.let {
            func(it)
        } ?: ResponseResult.NetworkError
        this.code() == 403 -> ResponseResult.AuthError
        else -> ResponseResult.NetworkError
    }
}

interface Difference {
    fun parseMessage(): String
}

sealed class ResponseResult {
    class AuthenticatedWithResult<T>(val result: T) : ResponseResult()
    object Authenticated : ResponseResult()
    object AuthError : ResponseResult()
    object NetworkError : ResponseResult()
}

fun ResponseResult.throwOnAuthError(): ResponseResult {
    if (this == ResponseResult.AuthError) throw AuthException()
    return this
}

fun ResponseResult.logOnNetworkError(): ResponseResult {
    if (this == ResponseResult.NetworkError) FirebaseCrashlytics.getInstance().recordException(AuthException())
    return this
}

fun sendNotification(applicationContext: Context, text: String, id: Int) {
    val intent = Intent(applicationContext, MainActivity::class.java)
    intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK

    val notificationManager =
        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    val bitmap = applicationContext.vectorToBitmap(R.drawable.ic_subjects)
    val titleNotification = applicationContext.getString(R.string.notification_title)
    val subtitleNotification = text
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
        channel.setSound(ringtoneManager, audioAttributes)
        notificationManager.createNotificationChannel(channel)
    }

    notificationManager.notify(id, notification.build())
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

fun String.getNameFromSender(): String {
    return if (this.contains("@")) {
        this.substringBefore("@").replace(".", " ")
    } else if (!this.contains(",")) {
        Pattern.compile("(\\w{2,}+\\.( ){1,})|(, \\w+)").matcher(this).replaceAll("")
    } else {
        this.substringBefore(",").substringAfterLast(". ")
    }
}

fun String.getInitialsFromName() = if (this.isEmpty()) "" else this.split(" ").filterNot { it.isEmpty() }.map {
    it.first().toUpperCase()
}.joinToString("")

fun getTextDrawable(text: String, seed: String, size: Int) =
    TextDrawable.builder().beginConfig().textColor(Color.WHITE).fontSize(size).bold()
        .toUpperCase()
        .endConfig()
        .buildRound(
            text,
            ColorGenerator.MATERIAL.getColor(seed)
        )

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Float.dp: Float
    get() = (this / Resources.getSystem().displayMetrics.density)
val Float.px: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

fun getImageUrl(id: String): String {
    val encoded = Base64Utils.encode(id.toByteArray(Charset.forName("UTF-8"))).toString().trim().replace("\n", "")
    return if (encoded == "OTcxMTA=") "https://i.giphy.com/media/IzwVNHuCkynOo/giphy.webp"
    else "https://is.stuba.sk/auth/lide/foto.pl?id=${id}"
}

fun getSuggestionRequestString(query: String) =
//    "_suggestKey=${query}&upresneni_default=aktivni_a_preruseni,absolventi,zamestnanci,externiste&_suggestMaxItems=25&vzorek=&_suggestHandler=lide&lang=undefined"
    "_suggestKey=${query}&upresneni_default=aktivni_a_preruseni,zamestnanci&_suggestMaxItems=25&vzorek=&_suggestHandler=lide&lang=undefined"


fun getSSLFactory(): SSLSocketFactory {
    val trustAllCerts: Array<TrustManager> = arrayOf(MyManager())
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null, trustAllCerts, SecureRandom())

    return sslContext.socketFactory
}

class MyManager : X509TrustManager {

    override fun checkServerTrusted(
        p0: Array<out java.security.cert.X509Certificate>?,
        p1: String?
    ) {
        //allow all
    }

    override fun checkClientTrusted(
        p0: Array<out java.security.cert.X509Certificate>?,
        p1: String?
    ) {
        //allow all
    }

    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
        return arrayOf()
    }
}

suspend fun <T> repeatIfException(times: Int, delay: Long, call: suspend () -> T): T? {
    repeat(times) {
        val response = runCatching { call() }
        delay(delay)
        if (response.isSuccess) {
            response.getOrNull()?.let {
                return it
            }
        } else {
            if (response.isFailure && response.exceptionOrNull() is AuthException) {
                throw AuthException()
            }
        }
    }
    return null
}

fun <T> List<T>.lastOrAll(condition: Boolean) = if (condition) this.takeLast(1) else this

fun <T> LiveData<T>.getDistinctBesidesNull(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastObj: T? = null
        override fun onChanged(obj: T?) {
            if (!initialized) {
                initialized = true
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            } else if (obj != lastObj) {
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            }
        }
    })
    return distinctLiveData.nonNull()
}