package com.mazeppa.firebasechat.registration

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mazeppa.firebasechat.MainActivity
import com.mazeppa.firebasechat.databinding.ActivitySetupProfileBinding
import com.mazeppa.firebasechat.model.User
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    private var _binding: ActivitySetupProfileBinding? = null
    private val binding: ActivitySetupProfileBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    private val startActivityForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.data?.let { uri ->
                binding.imageViewProfileImage.setImageURI(uri)
                selectedImage = uri

//                val time = Date().time
//                val reference = storage.reference
//                    .child("Profile")
//                    .child(time.toString())
//                reference.putFile(uri).addOnCompleteListener { task ->
//                    if (task.isSuccessful) {
//                        reference.downloadUrl.addOnCompleteListener { uri ->
//                            val filePath = uri.result.toString()
//                            val obj = HashMap<String, Any>()
//                            obj["image"] = filePath
//                            database.reference
//                                .child("users")
//                                .child(auth.uid!!)
//                                .updateChildren(obj)
//                                .addOnSuccessListener {
//
//                                }
//                        }
//                    }
//                }
            }
        }
    private lateinit var selectedImage: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setListeners()
    }

    private fun ActivitySetupProfileBinding.setListeners() {
        imageViewProfileImage.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }

            startActivityForResult.launch(intent)
        }

        buttonContinue.setOnClickListener {
            if (editTextUserName.text.toString().isEmpty()) {
                editTextUserName.error = "Name is empty"
            } else {
                if (::selectedImage.isInitialized) {
                    val reference = storage.reference.child("Profile")
                        .child(auth.uid!!)

                    reference.putFile(selectedImage).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnCompleteListener { uri ->
                                val imageUrl = uri.result.toString()
                                val uid = auth.uid
                                val phoneNumber = auth.currentUser!!.phoneNumber
                                val name = editTextUserName.text.toString()

                                val user = User(uid, name, phoneNumber, imageUrl)
                                database.reference
                                    .child("users")
                                    .child(uid!!)
                                    .setValue(user)
                                    .addOnCompleteListener {
                                        startActivity(
                                            Intent(
                                                this@SetupProfileActivity,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                            }
                        } else {
                            val uid = auth.uid
                            val name = editTextUserName.text.toString()
                            val phoneNumber = auth.currentUser?.phoneNumber

                            val user = User(uid, name, phoneNumber, "No Image")

                            database.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user)
                                .addOnCompleteListener {
                                    startActivity(
                                        Intent(
                                            this@SetupProfileActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}