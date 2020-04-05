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
import com.lukasanda.aismobile.util.*
import com.lukasanda.dataprovider.Parser
import com.lukasanda.dataprovider.data.EmailDetail
import com.lukasanda.dataprovider.data.Suggestion
import okhttp3.MediaType
import okhttp3.RequestBody
import org.joda.time.DateTime

class EmailRepository(
    private val emailDao: EmailDao,
    private val prefs: Prefs,
    private val service: AISApi
) {

    suspend fun update(updateType: UpdateType = UpdateType.Lazy): ResponseResult {

        var emailCount = 0

        val result = service.emails().authenticatedOrReturn { emailCountResponse ->
            val emailInfo = Parser.getEmailInfo(emailCountResponse)

            emailCount = emailInfo.emailPages
            prefs.sentDirectoryId = emailInfo.saveDirectoryId
            prefs.newEmailCount = emailInfo.newEmailCount

            ResponseResult.Authenticated
        }.logOnNetworkError()

        Log.d("TAG", "Email count: $emailCount")

        if (result == ResponseResult.AuthError) return result

        if (updateType == UpdateType.Lazy) {
            if (prefs.emailExpiration.isAfterNow)
                return ResponseResult.Authenticated
        }

        prefs.emailExpiration = DateTime.now().plusMinutes(10)

        val responses = (0 until emailCount).map { i ->
            service.emailPage(i.toString()).authenticatedOrReturn { response ->
                val emailsList = Parser.getEmails(response) ?: emptyList()
                emailDao.insertEmails(emailsList.map {
                    Email(
                        it.eid.substringAfter("="),
                        it.fid.substringAfter("="),
                        it.senderId,
                        it.sender,
                        it.subject,
                        it.date,
                        it.opened
                    )
                })
                ResponseResult.Authenticated
            }
        }

        return when {
            responses.all { it == ResponseResult.Authenticated } -> {
                ResponseResult.Authenticated
            }
            responses.contains(ResponseResult.AuthError) -> {
                ResponseResult.AuthError
            }
            else -> {
                ResponseResult.NetworkError
            }
        }
    }

    @Throws(AuthException::class, HTTPException::class)
    suspend fun delete(email: Email) {
        service.deleteEmail("${email.fid};eid=${email.eid};on=0;menu_akce=vymazat").authenticatedOrThrow()
        emailDao.deleteSingle(email)
    }

    suspend fun deleteAll() {
        emailDao.deleteAll()
    }

    suspend fun getEmailDetail(email: Email): EmailDetail {
        val response = service.emailDetail("${email.eid};fid=${email.fid};on=0").authenticatedOrThrow()
        val message = Parser.getEmailDetail(response)
        if (!email.opened) {
            prefs.newEmailCount--
        }
        emailDao.update(email.copy(opened = true))
        return message
    }

    fun getEmails() = emailDao.getEmails()

    suspend fun getSuggestions(query: String): List<Suggestion> {
        val suggestionResponse = service.getSuggestions(
            RequestBody.create(
                MediaType.parse("text/plain"),
                getSuggestionRequestString(query)
            )
        ).authenticatedOrThrow()

        return Parser.getSuggestions(suggestionResponse) ?: emptyList()
    }

    suspend fun sendMail(to: String, subject: String, message: String): Boolean {
        val newMailResponse = service.newMessagePage().authenticatedOrThrow()
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
        val newMailResponse = service.newMessagePage().authenticatedOrThrow()
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