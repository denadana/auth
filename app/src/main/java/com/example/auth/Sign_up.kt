package com.example.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.auth.databinding.ActivitySignUpBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class Sign_up : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    var imgUrl: Uri? = null
    var storge: FirebaseStorage? = null
    lateinit var progressDialog: ProgressDialog
    private var storageRef: StorageReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, Sign_in::class.java))
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
            val name = binding.TextFieldNameUp.editText!!.text
            val email = binding.TextFieldEmailUp.editText!!.text
            val phone = binding.TextFieldPhoneUp.editText!!.text
            val address = binding.TextFieldAddressUp.editText!!.text
            val birth = binding.TextFieldBirthDateUp.editText!!.text
            val password = binding.TextFieldPasswordUp.editText!!.text
            val comPassword = binding.TextFieldComPasswordUp.editText!!.text
            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty() && birth.isNotEmpty() && password.isNotEmpty() && comPassword.isNotEmpty() && imgUrl != null) {
                if (password.toString() == comPassword.toString()) {
                    Sign_up(
                        findViewById(android.R.id.content), password.toString(), User(
                            null,
                            name.toString(),
                            email.toString(),
                            phone.toString(),
                            address.toString(),
                            birth.toString(),
                            0,
                            "",
                            imgUrl.toString()
                        ), imgUrl
                    )
                } else {
                    Constants.showSnackBar(
                        findViewById(android.R.id.content),
                        "يجب ان تكون كلمة المرور وتأكيد كلمة المرور نفس البيانات",
                        Constants.redColor
                    )
                }

            } else {
                Constants.showSnackBar(
                    findViewById(android.R.id.content), "املأ جميع البيانات", Constants.redColor
                )
            }
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

    @SuppressLint("NotConstructor")
    fun Sign_up(view: View, password: String, users: User, imgeUri: Uri?) {
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(users.email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                createUser(
                    User(
                        auth.currentUser!!.uid,
                        users.name,
                        users.email,
                        users.address,
                        users.phone,
                        users.birth_date,
                        users.type,
                        users.fcm_token,
                        users.image,
                    ), imgeUri
                )
                Constants.showSnackBar(
                    view, "تم تسجيلك بنجاح", Constants.greenColor
                )
            } else {
                Constants.showSnackBar(
                    view, "فشل تسجيل الحساب", Constants.redColor
                )
            }
        }.addOnFailureListener {
            Log.e("error", it.message.toString())
        }
    }


    fun createUser(user: User, imgeUri: Uri?) {

        storge = Firebase.storage
        storageRef = storge!!.reference
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        db = Firebase.firestore
        imgeUri?.let {
            storageRef!!.child("image/" + auth.currentUser!!.uid).putFile(it)
                .addOnSuccessListener { taskSnapshot ->
                    progressDialog.dismiss()
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        user.image = uri.toString()
                        db.collection("users").document(auth.currentUser!!.uid).set(user)
                            .addOnSuccessListener {

                            }
                    }
                }.addOnFailureListener { exception ->
                    progressDialog.dismiss()
                    Log.e("exc", exception.message.toString())
                }.addOnProgressListener {
                    val progress: Double =
                        100.0 * it.bytesTransferred / it.totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }


    }
}