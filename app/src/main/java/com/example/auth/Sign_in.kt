package com.example.auth

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.auth.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class Sign_in : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignin.setOnClickListener {
            val email = binding.TextFieldEmailIn.editText!!.text
            val password = binding.TextFieldPasswordIn.editText!!.text
            if (email.isNotEmpty() && password.isNotEmpty()) {
                Sign_in(findViewById(android.R.id.content), email.toString(), password.toString())
            } else {
                Constants.showSnackBar(
                    findViewById(android.R.id.content),
                    "املأ جميع البيانات",
                    Constants.redColor
                )
            }
        }
    }


    @SuppressLint("NotConstructor")
    fun Sign_in(view: View, Email: String, Password: String) {
        auth = Firebase.auth
        db = Firebase.firestore
        auth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener {
            if (it.isSuccessful) {
                FirebaseMessaging.getInstance().subscribeToTopic(auth.currentUser!!.uid)
                db.collection("users").document(auth.currentUser!!.uid).get().addOnSuccessListener {
                    val users = it.toObject<User>()

                    val i = Intent(this, MainActivity::class.java)
                    i.putExtra("name", users!!.name)
                    i.putExtra("email", users!!.email)
                    i.putExtra("address", users!!.address)
                    i.putExtra("phone", users!!.phone)
                    i.putExtra("birth_date", users!!.birth_date)
                    i.putExtra("image", users!!.image)
                    startActivity(i)
                    finish()
                }

            } else {
                Constants.showSnackBar(
                    view,
                    "يرجى التحقق من اسم المستخدم أو كلمة السر الخاص بك",
                    Constants.redColor
                )
            }
        }
    }


}