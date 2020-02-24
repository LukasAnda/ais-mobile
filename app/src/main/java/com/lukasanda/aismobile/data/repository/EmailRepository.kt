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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasanda.aismobile.data.db.dao.EmailDao
import com.lukasanda.aismobile.data.db.entity.Email
import com.lukasanda.aismobile.data.remote.AuthException
import com.lukasanda.aismobile.data.remote.HTTPException
import com.lukasanda.aismobile.data.remote.api.AISApi
import com.lukasanda.aismobile.util.authenticatedOrThrow
import sk.lukasanda.dataprovider.Parser

class EmailRepository(private val emailDao: EmailDao, private val service: AISApi) {

    private val _emailDetail = MutableLiveData<String?>()
    fun emailDetail(): LiveData<String?> = _emailDetail
    fun clearDetail() = _emailDetail.postValue(null)

    @Throws(AuthException::class, HTTPException::class)
    suspend fun update() {
        val emailsCountResponse = service.emails().authenticatedOrThrow()
        val emailsCount = Parser.getEmailPages(emailsCountResponse)

        for (i in 0 until emailsCount) {
            val emailPageResponse = service.emailPage(i.toString()).authenticatedOrThrow()
            val emailsList = Parser.getEmails(emailPageResponse) ?: emptyList()
            println(emailsList.joinToString("\n"))
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
    }

    suspend fun getEmailDetail(email: Email) {
        val response =
            service.emailDetail("${email.eid};fid=${email.fid};on=0")
                .authenticatedOrThrow()
        val message = Parser.getEmailDetail(response)
        _emailDetail.postValue(message)
    }

    fun getEmails() = emailDao.getEmails()


}