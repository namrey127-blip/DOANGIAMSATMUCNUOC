package com.example.doangiamsatmucwater

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtEmail: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        // ===== ÁNH XẠ =====
        edtName = findViewById(R.id.edtName)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        edtEmail = findViewById(R.id.edtEmail)

        val btnSave = findViewById<Button>(R.id.btnSave)

        val btnBack = findViewById<Button>(R.id.btnBack)

        // ===== UID =====
        val uid =
            FirebaseAuth.getInstance().currentUser?.uid ?: return

        // ===== DATABASE =====
        val db = FirebaseDatabase.getInstance(
            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )

        val profileRef =
            db.getReference("SMART_HOME/users/$uid/profile")

        // ===== LOAD DATA =====
        profileRef.addListenerForSingleValueEvent(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    edtName.setText(
                        snapshot.child("name")
                            .getValue(String::class.java)
                    )

                    edtPhone.setText(
                        snapshot.child("phone")
                            .getValue(String::class.java)
                    )

                    edtAddress.setText(
                        snapshot.child("address")
                            .getValue(String::class.java)
                    )

                    edtEmail.setText(
                        snapshot.child("email")
                            .getValue(String::class.java)
                    )
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        // ===== SAVE =====
        btnSave.setOnClickListener {

            val map = HashMap<String, Any>()

            map["name"] =
                edtName.text.toString()

            map["phone"] =
                edtPhone.text.toString()

            map["address"] =
                edtAddress.text.toString()

            map["email"] =
                edtEmail.text.toString()

            profileRef.updateChildren(map)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Đã lưu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // ===== BACK =====
        btnBack.setOnClickListener {

            finish()
        }
    }
}