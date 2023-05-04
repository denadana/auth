package com.example.auth.Notification

import com.example.auth.Constants.Companion.CONTENT_TYPE1
import com.example.auth.Constants.Companion.SERVER_KEY
import com.example.auth.Model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization: key=$SERVER_KEY", "Content-type:$CONTENT_TYPE1")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}