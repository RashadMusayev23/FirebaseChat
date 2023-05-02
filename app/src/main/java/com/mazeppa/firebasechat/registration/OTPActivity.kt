package com.mazeppa.firebasechat.registration

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mazeppa.firebasechat.databinding.ActivityOtpactivityBinding
import com.mazeppa.firebasechat.util.IntentExtras
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private var _binding: ActivityOtpactivityBinding? = null
    private val binding: ActivityOtpactivityBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private lateinit var verificationId: String
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sendOTP()
        setListeners()
    }

    /**
     * Sends OTP to the phone number user entered in the [VerificationActivity] screen.
     */
    private fun sendOTP() {
        val phoneNumber = intent.getStringExtra(IntentExtras.PHONE_NUMBER) ?: ""
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                Toast.makeText(this@OTPActivity, "Verification Completed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@OTPActivity, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verifyId: String,
                forseResendingToken: PhoneAuthProvider.ForceResendingToken,
            ) {
                super.onCodeSent(verifyId, forseResendingToken)

                binding.progressBar.visibility = View.GONE
                binding.group.visibility = View.VISIBLE
                binding.editTextOtp.requestFocus()
                verificationId = verifyId
            }

        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun setListeners() {
        binding.buttonContinue.setOnClickListener {
            val credential =
                PhoneAuthProvider.getCredential(verificationId, binding.editTextOtp.text.toString())

            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this@OTPActivity, SetupProfileActivity::class.java))
                        finishAffinity()
                    } else {
                        Toast.makeText(this@OTPActivity, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}