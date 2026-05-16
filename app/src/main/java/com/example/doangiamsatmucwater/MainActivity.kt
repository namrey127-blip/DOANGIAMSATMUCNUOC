package com.example.doangiamsatmucwater

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    // --- BIẾN CỜ ĐỂ ĐÁNH DẤU VIỆC ÉP BƠM ---
    private var isOverride1 = false
    private var isOverride2 = false

    // --- BIẾN KIỂM SOÁT THÔNG BÁO HỆ THỐNG (Gửi 1 lần khi chạm ngưỡng) ---
    private var hasNotified1 = false
    private var hasNotified2 = false

    // --- BIẾN CÀI ĐẶT AN TOÀN ---
    private var isSafetyMode = true
    private var safetyLimit = 80.0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // --- 1. ĐIỀU HƯỚNG BOTTOM BAR ---
        findViewById<LinearLayout>(R.id.btnPage2).setOnClickListener {
            startActivity(Intent(this, trang2::class.java))
        }

        findViewById<LinearLayout>(R.id.btnPage3).setOnClickListener {
            startActivity(Intent(this, trang3::class.java))
        }

        // --- 2. ÁNH XẠ BỒN 1 ---
        val cardTankMain =
            findViewById<androidx.cardview.widget.CardView>(R.id.cardTankMain)

        val txtVolume = findViewById<TextView>(R.id.txtVolume)

        val txtDistance = findViewById<TextView>(R.id.txtDistance)

        val txtPumpStatus = findViewById<TextView>(R.id.txtPumpStatus)

        val txtMaxCap = findViewById<TextView>(R.id.txtMaxCap)

        val imgWaterWave = findViewById<ImageView>(R.id.imgWaterWave)

        val txtPercentInsideBottle =
            findViewById<TextView>(R.id.txtPercentInsideBottle)

        // --- 3. ÁNH XẠ BỒN 2 VÀ NÚT THÊM ---
        val cardTank2 =
            findViewById<androidx.cardview.widget.CardView>(R.id.cardTank2)

        val btnAddTank =
            findViewById<LinearLayout>(R.id.btnAddTank)

        val txtVolume2 = findViewById<TextView>(R.id.txtVolume2)

        val txtDistance2 = findViewById<TextView>(R.id.txtDistance2)

        val txtPumpStatus2 = findViewById<TextView>(R.id.txtPumpStatus2)

        val imgWaterWave2 = findViewById<ImageView>(R.id.imgWaterWave2)

        val txtPercentInsideBottle2 =
            findViewById<TextView>(R.id.txtPercentInsideBottle2)

        val scale = resources.displayMetrics.density

        // --- 4. HIỆU ỨNG SÓNG NƯỚC ---
        fun startWave(img: ImageView) {

            ObjectAnimator.ofFloat(
                img,
                "translationX",
                0f,
                -80f * scale
            ).apply {

                duration = 1500

                repeatCount = ValueAnimator.INFINITE

                interpolator = LinearInterpolator()

                start()
            }
        }

        startWave(imgWaterWave)
        startWave(imgWaterWave2)

        // --- 5. LOGIC CHUYỂN TRANG ---
        cardTankMain.setOnClickListener {

            startActivity(
                Intent(this, TankDetailActivity::class.java)
                    .putExtra("TANK_ID", "BON_1")
            )
        }

        btnAddTank.setOnClickListener {

            startActivity(Intent(this, AddTankActivity::class.java))
        }

        cardTank2.setOnClickListener {

            startActivity(
                Intent(this, TankDetailActivity::class.java)
                    .putExtra("TANK_ID", "BON_2")
            )
        }

        // --- 6. FIREBASE ---
        val uid =
            FirebaseAuth.getInstance().currentUser!!.uid

        database = FirebaseDatabase.getInstance(
            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("SMART_HOME/users/$uid/tanks")
        findViewById<Button>(R.id.btnLogout).setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            startActivity(
                Intent(this, LoginActivity::class.java)
            )

            finish()
        }
        // --- 7. CHỨC NĂNG BƠM VÀ XÓA BỒN ---
        val btnPump1 = findViewById<Button>(R.id.btnPump1)

        val btnStopPump1 = findViewById<Button>(R.id.btnStopPump1)

        val btnDelete1 = findViewById<Button>(R.id.btnDelete1)

        val btnPump2 = findViewById<Button>(R.id.btnPump2)

        val btnStopPump2 = findViewById<Button>(R.id.btnStopPump2)
        val btnChart1 =
            findViewById<Button>(R.id.btnChart1)

        val btnChart2 =
            findViewById<Button>(R.id.btnChart2)

        val btnDelete2 = findViewById<Button>(R.id.btnDelete2)

        val btnPage4 =
            findViewById<LinearLayout>(R.id.btnPage4)
        btnPage4.setOnClickListener {

            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }
        btnChart1.setOnClickListener {

            val i =
                Intent(this, ChartActivity::class.java)

            i.putExtra("tank", "BON_1")

            startActivity(i)
        }

        btnChart2.setOnClickListener {

            val i =
                Intent(this, ChartActivity::class.java)

            i.putExtra("tank", "BON_2")

            startActivity(i)
        }
        // ===== BỒN 1 =====
        btnPump1.setOnClickListener {
            database.child("BON_1").get().addOnSuccessListener { node ->
                val tankH = node.child("tank_height").value?.toString()?.toDoubleOrNull() ?: 1.0
                val dist = node.child("muc_nuoc").value?.toString()?.toDoubleOrNull() ?: 0.0

                var waterH = (tankH + 3.0) - dist
                if (waterH < 0.0) waterH = 0.0
                val percent = (waterH / tankH) * 100.0

                if (percent >= safetyLimit) {
                    val builder = android.app.AlertDialog.Builder(this)
                    builder.setTitle("⚠️ CẢNH BÁO TRÀN BỒN")
                    builder.setMessage("Mực nước hiện đã đạt ${percent.toInt()}%. Bạn có chắc chắn muốn tiếp tục?")
                    builder.setPositiveButton("Vẫn bơm") { _, _ ->
                        isOverride1 = true
                        database.child("BON_1/is_pumping").setValue(true)
                    }
                    builder.setNegativeButton("Dừng lại", null)
                    builder.show()
                } else {
                    isOverride1 = false
                    database.child("BON_1/is_pumping").setValue(true)
                }
            }
        }

        btnStopPump1.setOnClickListener {
            isOverride1 = false
            database.child("BON_1").child("is_pumping").setValue(false)
        }

        btnDelete1.setOnClickListener {
            database.child("BON_1").child("is_active").setValue(false)
        }

        // ===== BỒN 2 =====
        btnPump2.setOnClickListener {
            database.child("BON_2").get().addOnSuccessListener { node ->
                val tankH = node.child("tank_height").value?.toString()?.toDoubleOrNull() ?: 1.0
                val dist = node.child("muc_nuoc").value?.toString()?.toDoubleOrNull() ?: 0.0

                var waterH = (tankH + 3.0) - dist
                if (waterH < 0.0) waterH = 0.0
                val percent = (waterH / tankH) * 100.0

                if (percent >= safetyLimit) {
                    val builder = android.app.AlertDialog.Builder(this)
                    builder.setTitle("⚠️ CẢNH BÁO TRÀN BỒN")
                    builder.setMessage("Bồn 2 hiện đã đạt ${percent.toInt()}%. Bạn có chắc muốn bơm tiếp?")
                    builder.setPositiveButton("Vẫn bơm") { _, _ ->
                        isOverride2 = true
                        database.child("BON_2/is_pumping").setValue(true)
                    }
                    builder.setNegativeButton("Hủy", null)
                    builder.show()
                } else {
                    isOverride2 = false
                    database.child("BON_2/is_pumping").setValue(true)
                }
            }
        }

        btnStopPump2.setOnClickListener {
            isOverride2 = false
            database.child("BON_2").child("is_pumping").setValue(false)
        }

        btnDelete2.setOnClickListener {
            database.child("BON_2").child("is_active").setValue(false)
        }

        // --- 8. LẮNG NGHE DỮ LIỆU ---
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val settings = snapshot.child("settings")
                    isSafetyMode = settings.child("safety_mode").value as? Boolean ?: true
                    safetyLimit = settings.child("safety_limit").value?.toString()?.toDoubleOrNull() ?: 80.0

                    updateTankUI("BON_1", snapshot, txtVolume, txtDistance, txtMaxCap, txtPercentInsideBottle, imgWaterWave, txtPumpStatus, scale)

                    val bon2 = snapshot.child("BON_2")
                    if (bon2.child("is_active").value as? Boolean ?: false) {
                        cardTank2.visibility = View.VISIBLE
                        updateTankUI("BON_2", snapshot, txtVolume2, txtDistance2, null, txtPercentInsideBottle2, imgWaterWave2, txtPumpStatus2, scale)
                    } else {
                        cardTank2.visibility = View.GONE
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateTankUI(tankId: String, snapshot: DataSnapshot, txtVol: TextView, txtDist: TextView, txtMax: TextView?, txtPer: TextView, imgWave: ImageView, txtStatus: TextView, scale: Float) {
        val node = snapshot.child(tankId)
        val tankH = node.child("tank_height").value?.toString()?.toDoubleOrNull() ?: 1.0
        val maxC = node.child("max_capacity").value?.toString()?.toDoubleOrNull() ?: 1.0
        val dist = node.child("muc_nuoc").value?.toString()?.toDoubleOrNull() ?: 0.0

        var waterH = (tankH + 3.0) - dist
        if (waterH < 0.0) waterH = 0.0
        if (waterH > tankH) waterH = tankH
        val percent = (waterH / tankH) * 100.0
        val currentL = (percent / 100.0) * maxC

        val isPumping = node.child("is_pumping").value as? Boolean ?: false
        val autoMode = node.child("che_do_auto").value as? Boolean ?: false
        val currentOverride = if (tankId == "BON_1") isOverride1 else isOverride2

        // --- LOGIC THÔNG BÁO HỆ THỐNG (Không cần bật bơm vẫn báo) ---
        if (percent >= safetyLimit) {
            val alreadyNotified = if (tankId == "BON_1") hasNotified1 else hasNotified2
            if (!alreadyNotified) {
                sendSystemNotification(tankId, percent.toInt())
                if (tankId == "BON_1") hasNotified1 = true else hasNotified2 = true
            }
        }

        // --- LOGIC TỰ NGẮT (Chỉ chạy khi có bật an toàn) ---
        if (percent >= safetyLimit && isSafetyMode) {
            if (autoMode) {
                database.child(tankId).child("che_do_auto").setValue(false)
                database.child(tankId).child("is_pumping").setValue(false)
            } else if (isPumping && !currentOverride) {
                database.child(tankId).child("is_pumping").setValue(false)
            }
        }

        // Reset cờ để có thể báo lại lần sau khi nước rút xuống
        if (percent < safetyLimit - 5.0) {
            if (tankId == "BON_1") {
                hasNotified1 = false
                isOverride1 = false
            } else {
                hasNotified2 = false
                isOverride2 = false
            }
        }

        // --- 3. CẬP NHẬT GIAO DIỆN ---
        txtVol.text = String.format("%.1f L", currentL)
        txtDist.text = "Khoảng cách: ${dist} cm"
        txtMax?.text = "${maxC.toInt()} L"
        txtPer.text = "${percent.toInt()}%"

        val params = imgWave.layoutParams
        params.height = ((120 * percent / 100) * scale + 0.5f).toInt()
        imgWave.layoutParams = params

        val status = node.child("trang_thai_bom").value?.toString() ?: "OFF"
        if (status == "ON" || isPumping) {
            txtStatus.text = "BƠM: ĐANG CHẠY"
            txtStatus.setTextColor(Color.parseColor("#00B14F"))
        } else {
            txtStatus.text = "BƠM: ĐÃ DỪNG"
            txtStatus.setTextColor(Color.parseColor("#E53935"))
        }
    }

    private fun sendSystemNotification(tankId: String, percent: Int) {
        val channelId = "TANK_ALARM"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Cảnh báo bồn nước", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ CẢNH BÁO MỨC NƯỚC")
            .setContentText("$tankId hiện đang ở mức $percent%. Vui lòng kiểm tra!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(if (tankId == "BON_1") 1 else 2, builder.build())
    }
}