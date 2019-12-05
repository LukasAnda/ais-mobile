/*
 * Copyright 2019 Lukáš Anda. All rights reserved.
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



}