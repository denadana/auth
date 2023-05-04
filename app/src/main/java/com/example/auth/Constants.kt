package com.example.auth

import android.graphics.Color
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class Constants {
    companion object {
        const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY ="AAAALvvMH7c:APA91bExtzjgC1gSjwlV0hmQExQQFDoHTV5SdXMV7l3Pq9arnq6P9TTOniRNNQSfjXh_TfV5zj19Oup4_HGD0T7K8U-vc8-mLGWE1fOom4DB1PljsEIoB41vVTqRNWUPvEqRJPZKsULk" // get firebase server key from firebase project setting
//        const val SERVER_KEY ="AAAAmBnUpdc:APA91bGVg8RGTbt63ApiEnE28LQofHnzUPEaHuTWA1r5X38-fCj4q5Ovc7gRLtlSFlbhdeGDBwu8oI0VurIHwRnGu2am_o9jRj6dc0R3-vYzyjrNNPfRJGa3-XmaUjZBXFjPVFOi8T7c" // get firebase server key from firebase project setting
        const val auth = "AIzaSyCu84d4T90l7CagI6AQXsYIPO0Zg7yUZ7E"
        const val CONTENT_TYPE1 = "application/json"
        const val redColor = "#E30425"
        const val greenColor = "#1AD836"


        fun showSnackBar(view: View, title: String, color: String) {
            Snackbar.make(view, title, Snackbar.LENGTH_LONG).apply {
                animationMode = BaseTransientBottomBar.ANIMATION_MODE_SLIDE
                setBackgroundTint(Color.parseColor(color))
                setTextColor(Color.parseColor("#FFFFFF"))
                show()
            }// apply
        }// show snackBar
    }
}