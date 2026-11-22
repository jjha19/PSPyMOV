package com.example.myapplication.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.detalle.DetalleActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn = binding.button
        btn.setOnClickListener {
            val intent = Intent(this, DetalleActivity::class.java)
            intent.putExtra("id", 1)
            startActivity(intent)
        }

        val btn2 = binding.button2
        val texto1 = binding.textView
        btn2.setOnClickListener {
            texto1.text = "Texto cambiado"
        }
    }
}