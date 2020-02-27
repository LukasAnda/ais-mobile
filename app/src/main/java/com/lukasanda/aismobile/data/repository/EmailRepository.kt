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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.data.cache.Prefs
import com.lukasanda.aismobile.data.db.dao.EmailDao
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.authenticatedOrThrow
import com.lukasanda.aismobile.util.getSuggestionRequestString
import okhttp3.MediaType
import okhttp3.RequestBody
import org.joda.time.DateTime
import sk.lukasanda.dataprovider.Parser
import sk.lukasanda.dataprovider.data.Suggestion

class EmailRepository(
    private val emailDao: EmailDao,
    private val prefs: Prefs,
    private val service: AISApi
) {

    private val _emailDetail = MutableLiveData<String?>()
    fun emailDetail(): LiveData<String?> = _emailDetail
    fun clearDetail() = _emailDetail.postValue(null)


    @Throws(AuthException::class, HTTPException::class)
    suspend fun update(updateType: UpdateType = UpdateType.Lazy) {
        val emailsCountResponse = service.emails().authenticatedOrThrow()
        val emailsInfo = Parser.getEmailInfo(emailsCountResponse)

        val emailsCount = emailsInfo.emailPages
        prefs.sentDirectoryId = emailsInfo.saveDirectoryId
        prefs.newEmailCount = emailsInfo.newEmailCount

        if (updateType == UpdateType.Lazy) {
            if (prefs.emailExpiration.isAfterNow)
                return
        }

        for (i in 0 until emailsCount) {
            val emailPageResponse = service.emailPage(i.toString()).authenticatedOrThrow()
            val emailsList = Parser.getEmails(emailPageResponse) ?: emptyList()
            emailDao.insertEmails(emailsList.map {
                Email(
                    it.eid.substringAfter("="),
                    it.fid.substringAfter("="),
                    it.sender,
                    it.subject,
                    it.date,
                    it.opened
                )
            })
        }
        prefs.emailExpiration = DateTime.now().plusMinutes(15)
    }

    suspend fun getEmailDetail(email: Email) {
        val response =
            service.emailDetail("${email.eid};fid=${email.fid};on=0").authenticatedOrThrow()
        val message = Parser.getEmailDetail(response)
        _emailDetail.postValue(message)
        if (!email.opened) {
            prefs.newEmailCount--
        }
        emailDao.update(email.copy(opened = true))
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

    enum class UpdateType {
        Lazy, Purge
    }


}