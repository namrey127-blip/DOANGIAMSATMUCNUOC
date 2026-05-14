package com.example.doangiamsatmucwater

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class trang2 : AppCompatActivity() {

    private lateinit var listView: ListView

    private lateinit var list: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.trang2)

        // ===== ÁNH XẠ =====
        val btnBack = findViewById<Button>(R.id.btnBack)

        listView = findViewById(R.id.listLogs)

        // ===== LIST LOG =====
        list = ArrayList()

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            list
        )

        listView.adapter = adapter

        // ===== ĐỌC LOG FIREBASE =====
        FirebaseDatabase.getInstance(
            "https://mohamed-salah-6a04e-default-rtdb.asia-southeast1.firebasedatabase.app/"
        )
            .getReference("logs")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    list.clear()

                    for (s in snapshot.children) {

                        val text =
                            s.getValue(String::class.java)

                        if (text != null) {

                            list.add(text)
                        }
                    }

                    // log mới lên đầu
                    list.reverse()

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        // ===== QUAY LẠI =====
        btnBack.setOnClickListener {

            finish()
        }
    }
}