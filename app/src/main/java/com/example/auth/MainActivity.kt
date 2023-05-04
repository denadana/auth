package com.example.auth

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.auth.Model.NotificationData
import com.example.auth.Model.PushNotification
import com.example.auth.Model.SendToNotification
import com.example.auth.Notification.RetrofitInstance
import com.example.auth.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var imgUrl: Uri? = null
    var storge: FirebaseStorage? = null
    lateinit var progressDialog: ProgressDialog
    private var storageRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val name = intent.getStringExtra("name")
        val email = intent.getStringExtra("email")
        val address = intent.getStringExtra("address")
        val phone = intent.getStringExtra("phone")
        val birth_date = intent.getStringExtra("birth_date")
        val image = intent.getStringExtra("image")

        val name_edit = binding.TextFieldNameUp.editText!!.text
        val email_edit = binding.TextFieldEmailUp.editText!!.text
        val phone_edit = binding.TextFieldPhoneUp.editText!!.text
        val address_edit = binding.TextFieldAddressUp.editText!!.text
        val birth_edit = binding.TextFieldBirthDateUp.editText!!.text
        name_edit.append(name)
        email_edit.append(email)
        phone_edit.append(address)
        address_edit.append(phone)
        birth_edit.append(birth_date)
        Glide.with(this).load(image).into(binding.imgUserUp)

        Firebase.messaging.subscribeToTopic("general")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("sdasd", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }

        binding.imgUserUp.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(
                    1080,
                    1080
                )
                .start()
        }
        binding.btnSignup.setOnClickListener {

            if (name_edit.isNotEmpty() && email_edit.isNotEmpty() && phone_edit.isNotEmpty() && address_edit.isNotEmpty() && birth_edit.isNotEmpty()) {
                updateProfile(
                    binding.root, User(
                        auth.currentUser!!.uid,
                        name_edit.toString(),
                        email_edit.toString(),
                        phone_edit.toString(),
                        address_edit.toString(),
                        birth_edit.toString(),
                        0,
                        "",
                        imgUrl.toString()
                    ), imgUrl
                )
                val topic = "/topics/${auth.currentUser!!.uid}"
                val topic1 = "/topics/general"

                PushNotification(
                    NotificationData("massge", "asdasd"),
                    topic1
                ).also {
                    sendNotification(it)
                }
            } else {
                Constants.showSnackBar(
                    binding.root, "إملا الحقول المطلوبة", Constants.redColor
                )

            }

        }

//        PushNotification(
//            NotificationData("massge", "asdasd"),
//            topic
//        ).also {
//            sendNotification(it)
//        }


    }

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("sdasd", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("sdasd", response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Log.e("sdasd", e.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            imgUrl = data!!.data

            binding.imgUserUp.setImageURI(imgUrl)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateProfile(view: View, user: User, img: Uri?) {
        storge = Firebase.storage
        storageRef = storge!!.reference
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        db = Firebase.firestore
        auth = Firebase.auth

        val data = hashMapOf<String, Any?>(
            "name" to user.name,
            "email" to user.email,
            "address" to user.address,
            "phone" to user.phone,
            "birth_date" to user.birth_date
        )
        if (img != null) {
            storageRef!!.child("image/" + "${user.id}").putFile(img!!)
                .addOnSuccessListener { taskSnapshot ->
                    progressDialog.dismiss()
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        data.put("image", uri.toString())
                        db.collection("users").document(auth.currentUser!!.uid).update(data)
                            .addOnSuccessListener {
                                Constants.showSnackBar(
                                    view,
                                    "تم تعديل البروفايل",
                                    Constants.greenColor
                                )
                            }
                    }
                }.addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Constants.showSnackBar(
                        view,
                        "فشل تعديل البروفايل",
                        Constants.redColor
                    )
                }.addOnProgressListener {
                    val progress: Double =
                        100.0 * it.bytesTransferred / it.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        } else {
            db.collection("users").document(auth.currentUser!!.uid).update(data)
                .addOnSuccessListener {
                    progressDialog.dismiss()

                    Constants.showSnackBar(
                        view,
                        "تم تعديل البروفايل",
                        Constants.greenColor
                    )
                }

        }
    }
}