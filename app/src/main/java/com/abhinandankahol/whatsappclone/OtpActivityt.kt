package com.abhinandankahol.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abhinandankahol.whatsappclone.databinding.ActivityOtpActivitytBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class OtpActivityt : AppCompatActivity() {
    private val binding by lazy {
        ActivityOtpActivitytBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        verificationId = ""

        auth = FirebaseAuth.getInstance()
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Please Wait...").setTitle("Loading").setCancelable(false)
        dialog = builder.create()

        val phoneNum = "+91" + intent.getStringExtra("number")
        setUpOtp(phoneNum)

    }

    private fun setUpOtp(phoneNum: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum).setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Toast.makeText(
                        this@OtpActivityt,
                        p0.localizedMessage.toString(),
                        Toast.LENGTH_SHORT
                    )
                        .show()

                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(p0, p1)
                    dialog.dismiss()
                    verificationId = p0

                }

            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.verify.setOnClickListener {
            if (binding.enterotp.text!!.isEmpty()) {
                Toast.makeText(this, "Please Enter Otp", Toast.LENGTH_SHORT).show()
            } else {
                val credential = PhoneAuthProvider.getCredential(
                    verificationId,
                    binding.enterotp.text.toString()

                )
                auth.signInWithCredential(credential).addOnSuccessListener {
                    dialog.dismiss()
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                }
                    .addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(this, "Error${it.localizedMessage})", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }
}