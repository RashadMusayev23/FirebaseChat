package com.mazeppa.firebasechat.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.mazeppa.firebasechat.MainActivity
import com.mazeppa.firebasechat.R
import com.mazeppa.firebasechat.databinding.ActivityVerificationBinding
import com.mazeppa.firebasechat.util.IntentExtras


class VerificationActivity : AppCompatActivity() {

    private var _binding: ActivityVerificationBinding? = null
    private val binding: ActivityVerificationBinding
        get() = checkNotNull(_binding) {
            println("Binding is null")
        }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCurrentUser()
        setListeners()
        googleOneTapSignIn()
    }

    private fun googleOneTapSignIn() {
        oneTapClient = Identity.getSignInClient(this);
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build();
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
                    putExtra(IntentExtras.PHONE_NUMBER, phoneNumber)
                })
            } else {
                Toast.makeText(this@VerificationActivity, "Enter Phone Number", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.buttonGoogleSignIn.setOnClickListener {
            oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this@VerificationActivity) { result ->
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    activityLauncher.launch(intentSenderRequest)
                }
                .addOnFailureListener(this@VerificationActivity) { e ->
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    println(e.localizedMessage)
                }
        }
    }

    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with your backend.
                            println("Got ID token. EMAIL ${credential.id}")
                            println("Credential Phone Number${credential.phoneNumber}")
                            println("Credential Display Name${credential.displayName}")
                            println("Credential Password ${credential.password}")
                        }

                        else -> {
                            println("No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}