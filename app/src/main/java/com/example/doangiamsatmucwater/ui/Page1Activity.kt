package com.example.doangiamsatmucwater // Giữ nguyên package của fen

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Page1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page1) // Nối với giao diện Trang 1

        // Xử lý nút quay lại
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish() // Đóng trang này lại, nó sẽ tự lùi về MainActivity
        }
    }
}