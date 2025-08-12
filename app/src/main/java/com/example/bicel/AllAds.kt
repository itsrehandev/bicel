package com.example.bicel

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bicel.databinding.ActivityAllAdsBinding

class AllAds : AppCompatActivity() {
    private lateinit var binding : ActivityAllAdsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllAdsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialization

        //
        // Assignments

            //
        binding.searchBar.setOnClickListener{
            binding.searchView.show()
        }
        binding.searchView.editText.setOnEditorActionListener {
                v, actionId, event ->
            binding.searchView.hide()
            return@setOnEditorActionListener true
        }
    }
}