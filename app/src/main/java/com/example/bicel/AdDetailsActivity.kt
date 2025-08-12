package com.example.bicel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.adapters.AdDetailsViewPager
import com.example.bicel.databinding.ActivityAdDetailsBinding
import com.example.bicel.fragments.Chats
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding
    private lateinit var adDetailsViewPager: AdDetailsViewPager
    private lateinit var firestore: FirebaseFirestore
    private lateinit var uploaderName: String
    private lateinit var uploaderPicUri: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userViewModel : UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Variables
        val adData = intent?.getBundleExtra("adData")
        firebaseAuth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        // Ends
        userViewModel.isLoggedIn.observe(this){isLoggedIn->
            if(isLoggedIn || firebaseAuth.currentUser != null){
                binding.chatBtn.visibility = View.VISIBLE
                Log.d("ViewModellll","Logged In")
            }
            else{
                Log.d("ViewModellll","Logged Out")
                binding.chatBtn.visibility = View.GONE
            }

        }
        // Ads Images View Pager
        adDetailsViewPager = AdDetailsViewPager()
        binding.adImagesViewPager.adapter = adDetailsViewPager
        if(adData != null){
            // Getting Username And Profile pic uri
            val uploaderId = adData.getString("adUploader")

            firestore.collection("users").document(uploaderId!!).get().addOnSuccessListener {
                if (it.exists()){
                    uploaderName = it.getString("name")!!
                    uploaderPicUri = it.getString("profilePicUri")!!
                    binding.uploaderName.text = uploaderName
                    Glide.with(this).asBitmap().load(uploaderPicUri).into(binding.uploaderImg)
                }
            }
            lifecycleScope.launch {
                delay(100)
                if(uploaderId == firebaseAuth.currentUser?.uid){
                    binding.chatBtn.visibility = View.GONE
                }
            }
            // Disable Chat button if uploader is Current User
        // Retrieving Ad data
            val adPrice = adData.getString("adPrice")?.toInt()
        binding.adPrice.text = "Rs : ${ NumberFormat.getIntegerInstance(Locale.US).format(adPrice) }"
        binding.adTitle.text = adData.getString("adTitle")
        binding.adDiscription.text = adData.getString("adDescription")
            val list = adData.getStringArrayList("adImages")
            if (list != null) {
                adDetailsViewPager.setList(list)
                binding.adImagesCount.text = "1/${list.size}"
                binding.adImagesViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                        binding.adImagesCount.text =  "${position+1}/${list.size}"
                    }
                })
            }

        }
        binding.chatBtn.setOnClickListener {
            if (adData != null && uploaderPicUri.isNotEmpty()) {
                val uploaderId = adData.getString("adUploader")
                startActivity(Intent(this@AdDetailsActivity, ChatActivity::class.java)
                        .putExtra("receiverUid", uploaderId)
                    .putExtra("userName",uploaderName)
                    .putExtra("profilePicUri",uploaderPicUri)
                )
            }
        }
        binding.backBtn.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
    }
}