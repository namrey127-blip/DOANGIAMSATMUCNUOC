package com.example.doangiamsatmucwater

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TankDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseReference
    private var tankId: String = "BON_1" // Mặc định là Bồn 1 nếu không thấy ID
    private var hasNotified = false // CỜ CHỐNG SPAM: Giúp app không kêu liên tục

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tankdetail)

        // BƯỚC 1: Nhận ID bồn từ trang chủ gửi sang
        tankId = intent.getStringExtra("TANK_ID") ?: "BON_1"
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid
        // BƯỚC 2: Trỏ thẳng vào node của bồn đó (BON_1 hoặc BON_2)
        db = FirebaseDatabase.getInstance("https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("SMART_HOME/users/$uid/tanks/$tankId")
        val txtTitle = findViewById<TextView>(R.id.txtDetailTitle) // Nếu fen có TextView tiêu đề
        val txtDetailVolume = findViewById<TextView>(R.id.txtDetailVolume)
        val edtMaxCap = findViewById<EditText>(R.id.edtMaxCap)
        val edtHeight = findViewById<EditText>(R.id.edtHeight)
        val edtArea = findViewById<EditText>(R.id.edtArea)
        val edtNotifyPercent = findViewById<EditText>(R.id.edtNotifyPercent) // Ô nhập % báo động
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)

        // Hiển thị tên bồn đang chỉnh sửa
        txtTitle?.text = if (tankId == "BON_1") "CÀI ĐẶT BỒN 1" else "CÀI ĐẶT BỒN 2"

        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener { finish() }

        // ĐỌC DỮ LIỆU RIÊNG CỦA BỒN ĐƯỢC CHỌN
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val maxCap = snapshot.child("max_capacity").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val tankHeight = snapshot.child("tank_height").value?.toString()?.toDoubleOrNull() ?: 1.0
                    val distance = snapshot.child("muc_nuoc").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val notifyPercent = snapshot.child("notify_percent").value?.toString()?.toDoubleOrNull() ?: 100.0 // Đọc ngưỡng %

                    // Tính toán hiển thị nhanh (Bao gồm bù trừ 3cm gá cảm biến)
                    var waterH = (tankHeight + 3.0) - distance
                    if (waterH < 0.0) waterH = 0.0
                    if (waterH > tankHeight) waterH = tankHeight
                    val percent = (waterH / tankHeight) * 100.0
                    val currentL = (percent / 100.0) * maxCap

                    txtDetailVolume.text = String.format("Hiện có: %.1f L (%.1f%%)", currentL, percent)

                    // Điền dữ liệu vào ô nhập
                    if (!edtMaxCap.isFocused) edtMaxCap.setText(maxCap.toString())
                    if (!edtHeight.isFocused) edtHeight.setText(tankHeight.toString())
                    if (!edtArea.isFocused) edtArea.setText(snapshot.child("base_area").value?.toString() ?: "")
                    if (!edtNotifyPercent.isFocused) edtNotifyPercent.setText(notifyPercent.toString())

                    // LOGIC KIỂM TRA VÀ GỬI THÔNG BÁO
                    if (percent >= notifyPercent && notifyPercent > 0.0) {
                        if (!hasNotified) {
                            sendNotification(
                                if (tankId == "BON_1") "Cảnh báo Bồn 1" else "Cảnh báo Bồn 2",
                                "Nước đã chạm mức ${percent.toInt()}%, đạt ngưỡng cài đặt!"
                            )
                            hasNotified = true // Đánh dấu đã báo rồi
                        }
                    } else {
                        hasNotified = false // Reset lại cờ khi nước rút xuống dưới ngưỡng
                    }
                }
            }
            override fun onCancelled(p0: DatabaseError) {}
        })

        // LƯU DỮ LIỆU: Sẽ lưu vào đúng node BON_1 hoặc BON_2 đã trỏ ở trên
        btnUpdate.setOnClickListener {
            val updates = mapOf(
                "max_capacity" to (edtMaxCap.text.toString().toDoubleOrNull() ?: 0.0),
                "tank_height" to (edtHeight.text.toString().toDoubleOrNull() ?: 0.0),
                "base_area" to (edtArea.text.toString().toDoubleOrNull() ?: 0.0),
                "notify_percent" to (edtNotifyPercent.text.toString().toDoubleOrNull() ?: 100.0) // Lưu mức báo động
            )
            db.updateChildren(updates).addOnSuccessListener {
                Toast.makeText(this, "Đã cập nhật $tankId thành công!", Toast.LENGTH_SHORT).show()
            }
        }

        // Nút điều khiển bơm cho riêng bồn đó
        findViewById<Button>(R.id.btnOn).setOnClickListener { db.child("is_pumping").setValue(true) }
        findViewById<Button>(R.id.btnOff).setOnClickListener { db.child("is_pumping").setValue(false) }
    }

    // --- HÀM TẠO THÔNG BÁO PUSH NOTIFICATION ---
    private fun sendNotification(title: String, message: String) {
        val channelId = "TANK_ALARM_CHANNEL"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Yêu cầu của Android 8.0 trở lên: Phải tạo Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Cảnh báo tràn nước", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Icon mặc định của hệ thống
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}