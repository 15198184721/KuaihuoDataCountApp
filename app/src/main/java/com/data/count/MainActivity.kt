package com.data.count

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.data.count.databinding.ActivityMainBinding
import com.kuaihuo.data.count.dbmanager.DBConnectUtils

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<View>(R.id.connect).setOnClickListener {
            DBConnectUtils.connectDB()
        }
        findViewById<View>(R.id.close).setOnClickListener {

        }
    }
}