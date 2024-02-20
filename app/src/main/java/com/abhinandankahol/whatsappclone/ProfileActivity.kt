package com.abhinandankahol.whatsappclone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.abhinandankahol.whatsappclone.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                imageUri = it.data!!.data
                binding.profileIamge.setImageURI(imageUri)
            }
        }

        binding.profileIamge.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launcher.launch(intent)
        }

        binding.continueBtn.setOnClickListener {
            validation()
        }


    }

    private fun validation() {
        if (binding.EnterUsername.text!!.isEmpty()) {
            Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()

        } else {

            uploadData()

        }
    }

    private fun uploadData() {
        storage.reference.child("UserPorfiles/${UUID.randomUUID()}").putFile(imageUri!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener {
                    userData(it.toString())
                }
            }

    }

    private fun userData(image: String) {
        val user = UserModel(
            auth.currentUser!!.uid,
            binding.EnterUsername.text.toString(),
            auth.currentUser!!.phoneNumber.toString(),
            image
        )

        db.reference.child("users").child(auth.currentUser!!.uid).setValue(user)
            .addOnSuccessListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }


    }
}