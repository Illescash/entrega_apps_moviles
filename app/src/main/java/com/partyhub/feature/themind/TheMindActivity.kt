package com.partyhub.feature.themind

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.partyhub.databinding.ActivityTheMindBinding

class TheMindActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTheMindBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTheMindBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
