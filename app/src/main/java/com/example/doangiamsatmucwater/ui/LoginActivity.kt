package com.example.doangiamsatmucwater

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        auth.signOut()
        // ===== ĐÃ LOGIN =====


        val edtEmail =
            findViewById<EditText>(R.id.edtEmail)

        val edtPassword =
            findViewById<EditText>(R.id.edtPassword)

        val btnLogin =
            findViewById<Button>(R.id.btnLogin)

        val btnRegister =
            findViewById<Button>(R.id.btnRegister)

        // =================================================
        // LOGIN
        // =================================================
        btnLogin.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            val pass =
                edtPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nhập đầy đủ",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {

                    val uid =
                        auth.currentUser!!.uid

                    val db =
                        FirebaseDatabase.getInstance(
                            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
                        )

                    // =====================================
                    // GHI UID ĐANG ĐIỀU KHIỂN
                    // =====================================

                    db.getReference("SMART_HOME/current_device_uid")
                        .setValue(uid)

                    // =====================================
                    // OPTIONAL:
                    // current_device cho user hiện tại
                    // =====================================

                    db.getReference(
                        "SMART_HOME/users/$uid/current_device"
                    ).setValue(true)

                    Toast.makeText(
                        this,
                        "Đăng nhập thành công",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )

                    finish()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // =================================================
        // REGISTER
        // =================================================
        btnRegister.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            val pass =
                edtPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nhập đầy đủ",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener {

                    val uid =
                        auth.currentUser!!.uid

                    val db =
                        FirebaseDatabase.getInstance(
                            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
                        )

                    // =================================================
                    // USER ROOT
                    // =================================================
                    val userRef =
                        db.getReference("SMART_HOME/users/$uid")

                    // =================================================
                    // TẮT current_device TẤT CẢ USER
                    // =================================================
                    val usersRef =
                        db.getReference("SMART_HOME/users")

                    usersRef.get().addOnSuccessListener { snapshot ->

                        for (user in snapshot.children) {

                            user.ref.child("current_device")
                                .setValue(false)
                        }
                        // =================================================
// PROFILE
// =================================================
                        val profile = HashMap<String, Any>()

                        profile["name"] = "Người dùng mới"
                        profile["phone"] = ""
                        profile["address"] = ""
                        profile["email"] = email

                        userRef.child("profile")
                            .setValue(profile)

                        // =================================================
                        // BON 1
                        // =================================================
                        val bon1 = HashMap<String, Any>()

                        bon1["is_active"] = true
                        bon1["is_pumping"] = false
                        bon1["max_capacity"] = 250
                        bon1["muc_nuoc"] = 0
                        bon1["tank_height"] = 8
                        bon1["trang_thai_bom"] = "OFF"
                        bon1["che_do_auto"] = false
                        bon1["yeu_cau_ml"] = 0

                        // =================================================
                        // BON 2
                        // =================================================
                        val bon2 = HashMap<String, Any>()

                        bon2["is_active"] = false
                        bon2["is_pumping"] = false
                        bon2["max_capacity"] = 150
                        bon2["muc_nuoc"] = 0
                        bon2["tank_height"] = 8
                        bon2["trang_thai_bom"] = "OFF"
                        bon2["che_do_auto"] = false
                        bon2["yeu_cau_ml"] = 0

                        // =================================================
                        // GHI DỮ LIỆU
                        // =================================================

                        // tanks
                        userRef.child("tanks/BON_1")
                            .setValue(bon1)

                        userRef.child("tanks/BON_2")
                            .setValue(bon2)

                        // logs
                        val logs = HashMap<String, Any>()

                        logs["first_log"] = "Tạo tài khoản thành công"

                        userRef.child("logs")
                            .setValue(logs)

                        // =================================================
                        // current_device
                        // =================================================
                        userRef.child("current_device")
                            .setValue(true)

                        Toast.makeText(
                            this,
                            "Đăng ký thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(this, MainActivity::class.java)
                        )

                        finish()
                    }
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        it.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
        }}}