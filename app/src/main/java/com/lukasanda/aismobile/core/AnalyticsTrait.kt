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

package com.lukasanda.aismobile.core

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

const val ACTION_LOGIN = "action_login"
const val ACTION_EXIT = "action_exit"
const val ACTION_SHOW_COURSE_DETAIL = "action_course_detail"
const val ACTION_SHOW_EMAIL_DETAIL = "action_email_detail"
const val ACTION_COMPOSE_REPLY = "action_email_reply_compose"
const val ACTION_COMPOSE_EMAIL = "action_email_compose"
const val ACTION_COMPOSE_EMAIL_TO_TEACHER = "action_email_compose_teacher"
const val ACTION_COMPOSE_EMAIL_FROM_DETAIL = "action_email_compose_from_detail"
const val ACTION_SEND_REPLY = "action_email_send_reply"
const val ACTION_SEND_COMPOSED = "action_email_send_composed"
const val ACTION_EMAIL_DELETE = "actione_email_delete"
const val ACTION_OPEN_FOLDER = "action_documents_folder_open"
const val ACTION_OPEN_DOCUMENT = "action_documents_document_open"
const val ACTION_PEOPLE_DETAIL = "action_ppl_detail"
const val ACTION_LOGOUT = "action_logout"


const val EVENT_WRONG_PASSWORD = "event_wrong_password"

interface AnalyticsTrait {
    fun getAnalytics(): FirebaseAnalytics

    fun logEvent(eventName: String) {
        getAnalytics().logEvent(eventName, Bundle())
    }
}