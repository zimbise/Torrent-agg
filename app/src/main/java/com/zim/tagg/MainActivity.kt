package com.zim.tagg

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    private val vm: SearchViewModel by viewModels()
    private lateinit var adapter: ResultsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = findViewById<RecyclerView>(R.id.resultsList)
        list.layoutManager = LinearLayoutManager(this)
        adapter = ResultsAdapter(mutableListOf()) { item -> ClientDispatcher.dispatch(this, item) }
        list.adapter = adapter

        val searchBtn = findViewById<Button>(R.id.searchBtn)
        searchBtn.setOnClickListener {
            val q = findViewById<EditText>(R.id.searchInput).text.toString().trim()
            if (q.isNotEmpty()) vm.search(q)
        }
        searchBtn.setOnLongClickListener {
            startActivity(Intent(this, SettingsActivity::class.java)); true
        }

        vm.results.observe(this) { adapter.setData(it) }
    }
}
