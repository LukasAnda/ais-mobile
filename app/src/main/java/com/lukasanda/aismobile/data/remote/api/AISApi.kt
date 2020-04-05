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

package com.lukasanda.aismobile.data.remote.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AISApi {

    @FormUrlEncoded
    @POST("system/login.pl")
    suspend fun login(
        @Field("lang", encoded = true) lang: String = "sk",
        @Field("login_hidden", encoded = true) loginHidden: String = "1",
        @Field("destination", encoded = true) destination: String = "/auth/?lang=sk",
        @Field("auth_id_hidden", encoded = true) authIdHidden: String = "0",
        @Field("auth_2fa_type", encoded = true) auth2FaType: String = "no",
        @Field("credential_0", encoded = true) login: String,
        @Field("credential_1", encoded = true) password: String,
        @Field("credential_k", encoded = true) k: String = "",
        @Field("credential_2", encoded = true) validSeconds: String = "86400"
    ): Response<ResponseBody>


    @GET("auth/katalog/rozvrhy_view.pl")
    suspend fun schedule(
        @Query(
            "rozvrh_student_obec",
            encoded = true
        ) params: String = "1?zobraz=1;format=json;rozvrh_student=91984"
    ): Response<ResponseBody>

    @GET("auth/student/studium.pl")
    suspend fun educationInfo(): Response<ResponseBody>

    @GET("auth/wifi/heslo_vpn_sit.pl")
    suspend fun wifiInfo(): Response<ResponseBody>

    @GET("/auth/student/list.pl")
    suspend fun semesters(): Response<ResponseBody>

    @GET("/auth/student/list.pl")
    suspend fun subjects(
        @Query("studium", encoded = true) studium: String
//        @Field("studium", encoded = true) study: String, @Field(
//            "obdobi",
//            encoded = true
//        ) semester: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/student/list.pl")
    suspend fun subjectSheets(
        @Field("studium", encoded = true) study: String, @Field(
            "obdobi",
            encoded = true
        ) semester: String, @Field(
            "predmet",
            encoded = true
        ) predmet: String, @Field("zobraz_prubezne") zobrazPrubezne: String = "1"
    ): Response<ResponseBody>

    @POST("/auth/posta/slozka.pl")
    suspend fun emails(): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/posta/slozka.pl")
    suspend fun emailPage(@Field("on", encoded = true) page: String): Response<ResponseBody>

    @GET("/auth/posta/email.pl")
    suspend fun emailDetail(@Query("eid", encoded = true) params: String = "8851469;fid=620955;on=0"): Response<ResponseBody>

    @GET("/auth/posta/slozka.pl")
    suspend fun deleteEmail(@Query("fid", encoded = true) params: String): Response<ResponseBody>

    @Headers("Content-Type: application/x-www-form-urlencoded;charset=UTF-8")
    @POST("/auth/system/uissuggest.pl")
    suspend fun getSuggestions(@Body body: RequestBody): Response<ResponseBody>

    @POST("/auth/posta/nova_zprava.pl")
    suspend fun newMessagePage(): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/posta/nova_zprava.pl")
    suspend fun sendMessage(
        @Field("lang", encoded = true) lang: String = "sk",
        @Field("zprava", encoded = true) zprava: String = "1",
        @Field("To", encoded = true) to: String,
        @Field("Cc", encoded = true) copy: String = "",
        @Field("Bcc", encoded = true) hiddenCopy: String = "",
        @Field("Subject", encoded = true) subject: String,
        @Field("Data", encoded = true) message: String,
        @Field("priloha", encoded = true) priloha1: String = "",
        @Field("priloha", encoded = true) priloha2: String = "",
        @Field("priloha", encoded = true) priloha3: String = "",
        @Field("priloha", encoded = true) priloha4: String = "",
        @Field("priloha", encoded = true) priloha5: String = "",
        @Field("priloha", encoded = true) priloha6: String = "",
        @Field("priloha", encoded = true) priloha7: String = "",
        @Field("ulozit_odesl_zpravu", encoded = true) saveSentMessage: String = "1",
        @Field("ulozit_do_sl", encoded = true) saveMessageTo: String,
        @Field("send", encoded = true) sendMessage: String = "ODOSLAŤ SPRÁVU",
        @Field("akce", encoded = true) action: String = "schranka",
        @Field("serializace", encoded = true) serialisation: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("/auth/posta/nova_zprava.pl")
    suspend fun replyMessage(
        @Field("lang", encoded = true) lang: String = "sk",
        @Field("zprava", encoded = true) zprava: String = "1",
        @Field("To", encoded = true) to: String,
        @Field("Cc", encoded = true) copy: String = "",
        @Field("Bcc", encoded = true) hiddenCopy: String = "",
        @Field("Subject", encoded = true) subject: String,
        @Field("Data", encoded = true) message: String,
        @Field("priloha", encoded = true) priloha1: String = "",
        @Field("priloha", encoded = true) priloha2: String = "",
        @Field("priloha", encoded = true) priloha3: String = "",
        @Field("priloha", encoded = true) priloha4: String = "",
        @Field("priloha", encoded = true) priloha5: String = "",
        @Field("priloha", encoded = true) priloha6: String = "",
        @Field("priloha", encoded = true) priloha7: String = "",
        @Field("ulozit_odesl_zpravu", encoded = true) saveSentMessage: String = "1",
        @Field("ulozit_do_sl", encoded = true) saveMessageTo: String,
        @Field("send", encoded = true) sendMessage: String = "ODOSLAŤ SPRÁVU",
        @Field("akce", encoded = true) action: String = "schranka",
        @Field("serializace", encoded = true) serialisation: String,
        @Field("old_fid", encoded = true) oldFid: String,
        @Field("old_eid", encoded = true) oldEid: String,
        @Field("fid", encoded = true) fid: String,
        @Field("eid", encoded = true) eid: String,
        @Field("menu_akce", encoded = true) menu_action: String = "odpovedet"
    ): Response<ResponseBody>

    @GET("/auth/katalog/syllabus.pl")
    suspend fun getCourseDetail(@Query("predmet", encoded = true) predmet: String): Response<ResponseBody>

    @GET("/auth/dok_server/slozka.pl")
    suspend fun getDocumentsInFolder(@Query("ds", encoded = true) folder: String): Response<ResponseBody>

    @GET("/auth/dok_server/slozka.pl")
    suspend fun getDocumentInfo(@Query("id", encoded = true) id: String): Response<ResponseBody>

    @GET("/auth/lide/clovek.pl")
    suspend fun getPersonInfo(@Query("id", encoded = true) id: String): Response<ResponseBody>


}