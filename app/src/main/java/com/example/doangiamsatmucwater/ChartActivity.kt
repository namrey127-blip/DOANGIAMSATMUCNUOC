package com.example.doangiamsatmucwater

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chart)

        val chart =
            findViewById<LineChart>(R.id.lineChart)

        val btnBack =
            findViewById<Button>(R.id.btnBack)

        val tankName =
            intent.getStringExtra("tank") ?: "BON_1"

        val uid =
            FirebaseAuth.getInstance().currentUser?.uid ?: return

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Date())

        val ref = FirebaseDatabase.getInstance(
            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )
            .getReference(
                "SMART_HOME/users/$uid/history/$tankName/$date"
            )

        ref.addListenerForSingleValueEvent(
            object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val entries =
                        ArrayList<Entry>()

                    var index = 0f

                    for (s in snapshot.children) {

                        val value =
                            s.getValue(Float::class.java)

                        if (value != null) {

                            entries.add(
                                Entry(index, value)
                            )

                            index++
                        }
                    }

                    val dataSet =
                        LineDataSet(entries, "Mực nước")

                    dataSet.valueTextSize = 12f
                    dataSet.lineWidth = 3f

                    val lineData =
                        LineData(dataSet)

                    chart.data = lineData

                    chart.description.text =
                        "Biểu đồ hôm nay"

                    chart.animateX(1500)

                    chart.invalidate()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        btnBack.setOnClickListener {

            finish()
        }
    }
}