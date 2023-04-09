package com.example.testopengl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testopengl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.glView.setEGLContextClientVersion(3)
        binding.glView.setRenderer(TestRenderer(this))
    }
}
