package com.partyhub.feature.elas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.partyhub.databinding.ActivityElAsBinding

class ElAsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityElAsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElAsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
