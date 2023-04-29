package com.mazeppa.firebasechat.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mazeppa.firebasechat.MainActivity
import com.mazeppa.firebasechat.databinding.ActivityVerificationBinding

class VerificationActivity : AppCompatActivity() {

    private var _binding: ActivityVerificationBinding? = null
    private val binding : ActivityVerificationBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCurrentUser()
        setListeners()
    }

    /**
     * Checks whether user is registered, if so, opens MainActivity
     */
    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setListeners() {
        binding.buttonContinue.setOnClickListener {
            val phoneNumber = binding.editTextPhoneNumber.text.toString()

            if (phoneNumber.isNotEmpty()) {
                startActivity(Intent(this@VerificationActivity, OTPActivity::class.java).apply {
                    putExtra("phoneNumber", phoneNumber)
                })
            } else {
                Toast.makeText(this@VerificationActivity, "Enter Phone Number", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}