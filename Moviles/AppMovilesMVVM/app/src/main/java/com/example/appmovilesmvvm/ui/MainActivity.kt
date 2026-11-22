package com.example.appmovilesmvvm.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appmovilesmvvm.R
import com.example.appmovilesmvvm.databinding.ActivityMainBinding
import com.example.appmovilesmvvm.di.AppModule

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.MainViewModelFactory(AppModule.damePresidenteUseCase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observarEstado()
        setupEventos()
    }

    fun observarEstado() {
        viewModel.state.observe(this, {
            it?.presidente.let {
                binding.textonator.text = it
            }
            it.error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setupEventos(){
        binding.botonator.setOnClickListener {
            viewModel.handleDamePresidente()
        }
    }
}