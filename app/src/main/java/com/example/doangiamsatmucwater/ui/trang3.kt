package com.example.doangiamsatmucwater

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class trang3 : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // Biến lưu cài đặt an toàn (mặc định)
    private var isSafetyMode = true
    private var safetyLimit = 80.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.trang3)

        // --- 1. ÁNH XẠ CÀI ĐẶT AN TOÀN ---
        val switchSafetyMode = findViewById<Switch>(R.id.switchSafetyMode)
        val edtSafetyLimit = findViewById<EditText>(R.id.edtSafetyLimit)
        val btnSaveLimit = findViewById<Button>(R.id.btnSaveLimit)

        // --- 2. ÁNH XẠ BỒN 1 ---
        val switchAuto1 = findViewById<Switch>(R.id.switchAuto1)
        val txtLevel1 = findViewById<TextView>(R.id.txtLevel1)
        val txtStatus1 = findViewById<TextView>(R.id.txtStatus1)
        val edtMl1 = findViewById<EditText>(R.id.edtMl1)
        val btnPump1 = findViewById<Button>(R.id.btnPump1)

        // --- 3. ÁNH XẠ BỒN 2 ---
        val switchAuto2 = findViewById<Switch>(R.id.switchAuto2)
        val txtLevel2 = findViewById<TextView>(R.id.txtLevel2)
        val txtStatus2 = findViewById<TextView>(R.id.txtStatus2)
        val edtMl2 = findViewById<EditText>(R.id.edtMl2)
        val btnPump2 = findViewById<Button>(R.id.btnPump2)
        val cardTank2 = findViewById<View>(R.id.cardTank2)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        val firebaseUrl =
            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"

        database = FirebaseDatabase.getInstance(firebaseUrl)
            .getReference("SMART_HOME")
            .child("users")
            .child("$uid")
            .child("tanks")

        // --- 4. LẮNG NGHE DỮ LIỆU ---
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Đọc cài đặt an toàn từ node settings
                    val settings = snapshot.child("settings")
                    isSafetyMode = settings.child("safety_mode").value as? Boolean ?: true
                    safetyLimit = settings.child("safety_limit").value?.toString()?.toDoubleOrNull() ?: 80.0

                    // Cập nhật giao diện cài đặt (chỉ cập nhật khi user không đang nhập)
                    switchSafetyMode.isChecked = isSafetyMode
                    if (!edtSafetyLimit.isFocused) {
                        edtSafetyLimit.setText(safetyLimit.toInt().toString())
                    }

                    // Xử lý Bồn 1
                    checkAndProtectTank("BON_1", snapshot, txtLevel1, txtStatus1, switchAuto1)

                    // Xử lý Bồn 2 (Ẩn/Hiện dựa trên is_active)
                    val isActive2 = snapshot.child("BON_2/is_active").value as? Boolean ?: false
                    if (isActive2) {
                        cardTank2.visibility = View.VISIBLE
                        checkAndProtectTank("BON_2", snapshot, txtLevel2, txtStatus2, switchAuto2)
                    } else {
                        cardTank2.visibility = View.GONE
                    }
                }
            }

            private fun checkAndProtectTank(
                tankId: String,
                snapshot: DataSnapshot,
                txtLevel: TextView,
                txtStatus: TextView,
                swAuto: Switch
            ) {
                val node = snapshot.child(tankId)
                val tankH = node.child("tank_height").value?.toString()?.toDoubleOrNull() ?: 100.0
                val dist = node.child("muc_nuoc").value?.toString()?.toDoubleOrNull() ?: 0.0
                val autoMode = node.child("che_do_auto").value as? Boolean ?: false
                val isPumping = node.child("is_pumping").value as? Boolean ?: false

                var waterH = (tankH + 3.0) - dist
                if (waterH < 0.0) waterH = 0.0
                if (waterH > tankH) waterH = tankH
                val percent = (waterH / tankH) * 100.0

                txtLevel.text = String.format("%.1f%%", percent)
                val statusStr = node.child("trang_thai_bom").value?.toString() ?: "OFF"
                txtStatus.text = statusStr
                txtStatus.setTextColor(if(statusStr.contains("ON")) Color.parseColor("#00B14F") else Color.RED)

                if (!swAuto.isPressed) swAuto.isChecked = autoMode

                // LOGIC TỰ NGẮT THEO NGƯỠNG TÙY CHỈNH
                if (percent >= safetyLimit && isSafetyMode) {
                    if (isPumping || autoMode) {
                        val stopUpdates = HashMap<String, Any>()
                        stopUpdates["$tankId/is_pumping"] = false
                        stopUpdates["$tankId/che_do_auto"] = false
                        stopUpdates["$tankId/yeu_cau_ml"] = 0

                        // Ép dừng ngay lập tức
                        database.updateChildren(stopUpdates)

                        // Cập nhật giao diện Switch ngay tại chỗ để không bị nhảy ngược
                        swAuto.isChecked = false
                        Toast.makeText(this@trang3, "HỆ THỐNG AN TOÀN ĐÃ NGẮT $tankId", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // --- 5. ĐIỀU KHIỂN CÀI ĐẶT AN TOÀN ---
        switchSafetyMode.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                database.child("settings/safety_mode").setValue(isChecked)
            }
        }

        btnSaveLimit.setOnClickListener {
            val input = edtSafetyLimit.text.toString().toDoubleOrNull()
            if (input != null && input in 1.0..100.0) {
                database.child("settings/safety_limit").setValue(input)
                Toast.makeText(this, "Đã lưu ngưỡng ngắt: $input%", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Vui lòng nhập từ 1 đến 100", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 6. ĐIỀU KHIỂN AUTO (Bật Auto là bật bơm) ---
        switchAuto1.setOnClickListener {
            val isChecked = switchAuto1.isChecked
            val updates = HashMap<String, Any>()

            if (isChecked) {
                updates["BON_1/che_do_auto"] = true
                updates["BON_1/is_pumping"] = true
                Toast.makeText(this, "Đã bật Auto Bồn 1", Toast.LENGTH_SHORT).show()
            } else {
                updates["BON_1/che_do_auto"] = false
                updates["BON_1/is_pumping"] = false
                updates["BON_1/yeu_cau_ml"] = 0
                Toast.makeText(this, "Đã tắt Auto Bồn 1", Toast.LENGTH_SHORT).show()
            }
            // Gửi tất cả lệnh cùng 1 lúc, Relay sẽ nhận chuẩn hơn
            database.updateChildren(updates)
        }

        switchAuto2.setOnClickListener {
            val isChecked = switchAuto2.isChecked
            val updates = HashMap<String, Any>()

            if (isChecked) {
                updates["BON_2/che_do_auto"] = true
                updates["BON_2/is_pumping"] = true
            } else {
                updates["BON_2/che_do_auto"] = false
                updates["BON_2/is_pumping"] = false
                updates["BON_2/yeu_cau_ml"] = 0
            }
            database.updateChildren(updates)
        }
        // --- 7. BƠM THỦ CÔNG THEO ML ---
        btnPump1.setOnClickListener {
            val mlValue = edtMl1.text.toString().toIntOrNull() ?: 0
            if (mlValue > 0) {
                database.child("BON_1/is_pumping").setValue(true)
                database.child("BON_1/yeu_cau_ml").setValue(mlValue)
                edtMl1.text.clear()
            }
        }

        btnPump2.setOnClickListener {
            val mlValue = edtMl2.text.toString().toIntOrNull() ?: 0
            if (mlValue > 0) {
                database.child("BON_2/is_pumping").setValue(true)
                database.child("BON_2/yeu_cau_ml").setValue(mlValue)
                edtMl2.text.clear()
            }
        }
    }
}