package com.example.doangiamsatmucwater

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddTankActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tank)

        val edtMaxCap = findViewById<EditText>(R.id.edtMaxCap2)
        val edtHeight = findViewById<EditText>(R.id.edtHeight2)
        val edtArea = findViewById<EditText>(R.id.edtArea2)
        val btnConfirmAdd = findViewById<Button>(R.id.btnConfirmAdd)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseDatabase.getInstance("https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("SMART_HOME/users/$uid/tanks/BON_2")

        btnConfirmAdd.setOnClickListener {
            val maxCap = edtMaxCap.text.toString().toDoubleOrNull()
            val height = edtHeight.text.toString().toDoubleOrNull()
            val area = edtArea.text.toString().toDoubleOrNull()

            if (maxCap != null && height != null && area != null) {
                // Đẩy thông số lên Firebase và BẬT CỜ is_active
                val updates = mapOf(
                    "max_capacity" to maxCap,
                    "tank_height" to height,
                    "base_area" to area,
                    "is_active" to true // Bí quyết để thẻ Bồn 2 hiện ra ở Trang chủ
                )
                db.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(this, "Đã kích hoạt Bồn số 2 thành công!", Toast.LENGTH_SHORT).show()
                    finish() // Đóng trang này, quay về trang chủ sẽ thấy bồn 2 xuất hiện!
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ vào các ô!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}