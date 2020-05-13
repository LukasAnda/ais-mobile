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

package com.lukasanda.aismobile.data.repository

import android.util.Log
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.EmailDao
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.ResponseResult
import com.lukasanda.aismobile.util.authenticatedOrThrow2
import com.lukasanda.aismobile.util.getSuggestionRequestString
import com.lukasanda.aismobile.util.repeatIfException
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.EmailDetail
import com.lukasanda.dataprovider.data.Suggestion
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

class EmailRepository(
    private val emailDao: EmailDao,
    private val prefs: Prefs,
    private val service: AISApi
) {

    suspend fun update2(updateType: UpdateType = UpdateType.Lazy): ResponseResult {


        runCatching {
            repeatIfException(5, 2000) { service.emails().authenticatedOrThrow2() }
                ?.let { Parser.getEmailInfo(it) }
                ?.let {
                    prefs.sentDirectoryId = it.saveDirectoryId
                    prefs.newEmailCount = it.newEmailCount
                    (0 until it.emailPages)
                }
                ?.takeUnless { updateType == UpdateType.Lazy && prefs.emailExpiration.isAfterNow }

                ?.map {
                    Timber.d("Updating page #$it")
                    repeatIfException(5, 2000) { service.emailPage(it.toString()).authenticatedOrThrow2() }
                }
                ?.filterNotNull()
                ?.map { Parser.getEmails(it) }
                ?.filterNotNull()
                ?.flatten()
                ?.map {
                    Email(
                        it.eid.substringAfter("="),
                        it.fid.substringAfter("="),
                        it.senderId,
                        it.sender,
                        it.subject,
                        it.date,
                        it.opened
                    )
                }
                ?.let {
                    val latestMail = emailDao.getEmailsList().maxBy {
                        DateTime.parse(
                            it.date,
                            DateTimeFormat.forPattern("dd. MM. yyyy HH:mm")
                        )
                    }

                    Timber.d("Inserting ${it.size} messages")

                    emailDao.update(it)

                    it.sortedByDescending {
                        DateTime.parse(
                            it.date,
                            DateTimeFormat.forPattern("dd. MM. yyyy HH:mm")
                        )
                    }.indexOfFirst { latestMail != null && it.eid == latestMail.eid && it.fid == latestMail.fid }
                        .takeIf { it > 0 }
                        ?.also { return ResponseResult.AuthenticatedWithResult(it) }
                }
        }.exceptionOrNull()?.takeIf { it is AuthException }?.let {
            return ResponseResult.AuthError
        }

        return ResponseResult.Authenticated

    }


    @Throws(AuthException::class, HTTPException::class)
    suspend fun delete(email: Email) {
        service.deleteEmail("${email.fid};eid=${email.eid};on=0;menu_akce=vymazat").authenticatedOrThrow2()
        emailDao.deleteSingle(email)
    }

    suspend fun deleteAll() {
        emailDao.deleteAll()
    }

    suspend fun getEmailDetail(email: Email): EmailDetail {
        val response = service.emailDetail("${email.eid};fid=${email.fid};on=0").authenticatedOrThrow2()
        val message = Parser.getEmailDetail(response)
        if (!email.opened) {
            prefs.newEmailCount--
        }
        emailDao.update(email.copy(opened = true))
        return message
    }

    fun getEmails() = emailDao.getAllEmails()

    suspend fun getSuggestions(query: String): List<Suggestion> {
        val suggestionResponse = service.getSuggestions(
            RequestBody.create(
                "text/plain".toMediaType(),
                getSuggestionRequestString(query)
            )
        ).authenticatedOrThrow2()

        return Parser.getSuggestions(suggestionResponse) ?: emptyList()
    }

    suspend fun sendMail(to: String, subject: String, message: String): Boolean {
        val newMailResponse = service.newMessagePage().authenticatedOrThrow2()
        val token = Parser.getNewMessageToken(newMailResponse)
        if (token.isEmpty()) {
            Log.d("TAG", "Empty token, something went wrong")
            return false
        } else {
            Log.d("TAG", "Sending message")
        }

        val response = service.sendMessage(
            to = to,
            subject = subject,
            message = message,
            saveMessageTo = prefs.sentDirectoryId,
            serialisation = token
        )

        return response.code() == 302
    }

    suspend fun replyMail(
        to: String,
        subject: String,
        message: String,
        originalEmail: Email
    ): Boolean {
        val newMailResponse = service.newMessagePage().authenticatedOrThrow2()
        val token = Parser.getNewMessageToken(newMailResponse)
        if (token.isEmpty()) {
            Log.d("TAG", "Empty token, something went wrong")
            return false
        } else {
            Log.d("TAG", "Sending message")
        }

        val response = service.replyMessage(
            to = to,
            subject = subject,
            message = message,
            saveMessageTo = prefs.sentDirectoryId,
            serialisation = token,
            oldEid = originalEmail.eid,
            oldFid = originalEmail.fid,
            eid = originalEmail.eid,
            fid = originalEmail.fid
        )

        return response.code() == 302
    }

    enum class UpdateType {
        Lazy, Purge
    }
}