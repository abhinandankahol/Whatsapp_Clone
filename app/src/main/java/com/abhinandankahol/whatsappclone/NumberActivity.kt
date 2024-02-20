package com.abhinandankahol.whatsappclone

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abhinandankahol.whatsappclone.databinding.ActivityNumberBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class NumberActivity : AppCompatActivity() {

    private val binding by lazy { ActivityNumberBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Firebase.auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.verify.setOnClickListener {
            validateUser()
        }
    }

    private fun validateUser() {

        if (binding.mobilenum.text!!.isEmpty()) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, OtpActivityt::class.java)
            intent.putExtra("number", binding.mobilenum.text.toString())
            startActivity(intent)

        }

    }
}