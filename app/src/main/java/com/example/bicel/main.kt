package com.example.bicel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.bicel.SharedPreferences.SharedPreferencesClass
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.adapters.StartupImgAdapter
import com.example.bicel.databinding.DiscardAdDialogBinding
import com.example.bicel.databinding.FragmentMainBinding
import com.example.bicel.fragments.Account
import com.example.bicel.fragments.Chats
import com.example.bicel.fragments.Home
import com.example.bicel.fragments.MyAds
import com.example.bicel.fragments.Sell

class main : Fragment() {
    lateinit var binding: FragmentMainBinding
    private val homeFragment = Home()
    private val chatsFragment = Chats()
    val sellFragment = Sell()
    private lateinit var sharedPreferences: SharedPreferencesClass
    private val myAdsFragment = MyAds()
    private val accountFragment = Account()
    private lateinit var currentFragment: Fragment
    lateinit var userViewModel: UserViewModel
    private lateinit var startupAdapter: StartupImgAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startupAdapter = StartupImgAdapter()
        sharedPreferences = SharedPreferencesClass(requireContext())

        if (sharedPreferences.isFirstLoad()) {
            binding.startupCont.visibility = View.VISIBLE
            binding.bottomNavView.visibility = View.GONE
            binding.fragmentContainer.visibility = View.GONE

            binding.startupRv.adapter = startupAdapter
            val imgs = listOf(R.drawable.startup_one, R.drawable.startup_two, R.drawable.startup_three)
            startupAdapter.setImgs(imgs)

            binding.nextBtn.setOnClickListener {
                val currentPosition = binding.startupRv.currentItem
                if (currentPosition < imgs.size - 1) { // If nothing is completely visible
                    binding.startupRv.currentItem = currentPosition + 1
                } else {
                    binding.startupCont.visibility = View.GONE
                    binding.bottomNavView.visibility = View.VISIBLE
                    binding.fragmentContainer.visibility = View.VISIBLE
                    sharedPreferences.setFirstTimeLaunch(false)
                }
//                dotsWorking(currentPosition)

            }
            // Skip Button
            binding.skipBtn.setOnClickListener {
                binding.startupCont.visibility = View.GONE
                binding.bottomNavView.visibility = View.VISIBLE
                binding.fragmentContainer.visibility = View.VISIBLE
                sharedPreferences.setFirstTimeLaunch(false)
            }
            // Block forward swipe
            var startX = 0f
            binding.startupRv.getChildAt(0).setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> startX = event.x
                    MotionEvent.ACTION_MOVE -> {
                        val diffX = event.x - startX
                        if (diffX < 0) {
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
            binding.startupRv.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    dotsWorking(position)
                }
            })
        } else {
            binding.startupCont.visibility = View.GONE
            binding.bottomNavView.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.VISIBLE
        }
        // Variables
        userViewModel = ViewModelProvider(requireActivity()).get(
            UserViewModel::
            class.java
        )
        // Ends

        initialFragmentTransaction()
    }


    private fun initialFragmentTransaction() {
        val fm = childFragmentManager // Used when fragments are managed within a fragment
        try {
            fm.findFragmentByTag("chats")?.let { // Remove Fragment if by chance already Created
                fm.beginTransaction().remove(it).commit()
            }
            fm.findFragmentByTag("sell")?.let {
                fm.beginTransaction().remove(it).commit()
            }
            fm.findFragmentByTag("myAds")?.let {
                fm.beginTransaction().remove(it).commit()
            }
            fm.findFragmentByTag("account")?.let {
                fm.beginTransaction().remove(it).commit()
            }
            fm.findFragmentByTag("home")?.let {
                fm.beginTransaction().remove(it).commit()
            }
        } catch (_: java.lang.Exception) {
        } catch (_: Exception) {
        }
        fm.beginTransaction().add(binding.fragmentContainer.id, chatsFragment, "chats")
            .hide(chatsFragment).commit()
        fm.beginTransaction().add(binding.fragmentContainer.id, sellFragment, "sell")
            .hide(sellFragment).commit()
        fm.beginTransaction().add(binding.fragmentContainer.id, myAdsFragment, "myAds")
            .hide(myAdsFragment).commit()
        fm.beginTransaction().add(binding.fragmentContainer.id, accountFragment, "account")
            .hide(accountFragment).commit()
        fm.beginTransaction().add(binding.fragmentContainer.id, homeFragment, "home").commit()

        currentFragment = homeFragment


        val isChatFrag = activity?.intent?.getBundleExtra("chatBundle")?.getBoolean("chat")
        if (isChatFrag != null) {
            goToFragment(chatsFragment)
            binding.bottomNavView.selectedItemId = R.id.chats
        } else {
            binding.bottomNavView.selectedItemId = R.id.home
        }


        userViewModel.isUploadingAd.observe(viewLifecycleOwner) { isuploading ->

            binding.bottomNavView.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.home -> {
                        goToFragment(homeFragment)
                        if (isuploading) return@setOnItemSelectedListener false // Don't select it
                        else return@setOnItemSelectedListener true // Select it

                    }

                    R.id.chats -> {
                        goToFragment(chatsFragment)
                        if (isuploading) return@setOnItemSelectedListener false
                        else return@setOnItemSelectedListener true

                    }

                    R.id.sell -> {
                        if (isuploading) {
                            return@setOnItemSelectedListener false // Don't select it
                        } else {
                            goToFragment(sellFragment) // if not in uploading state then select it and go to sell fragment
                            return@setOnItemSelectedListener true
                        }

                    }

                    R.id.myads -> {
                        goToFragment(myAdsFragment)
                        if (isuploading) return@setOnItemSelectedListener false
                        else return@setOnItemSelectedListener true

                    }

                    R.id.account -> {
                        goToFragment(accountFragment)
                        if (isuploading) return@setOnItemSelectedListener false
                        else return@setOnItemSelectedListener true
                    }
                }
                true
            }


        }

    }

    fun goToFragment(fragment: Fragment) {
        try {
            if (currentFragment == sellFragment && userViewModel.isUploadingAd.value!!) {
                val dialogBoxBinding: DiscardAdDialogBinding =
                    DiscardAdDialogBinding.inflate(layoutInflater)
                val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                    .setView(dialogBoxBinding.root)
                val discardDialogBox = dialogBuilder.create()
                discardDialogBox.apply {
                    setCancelable(false)
                    show()
                }

                dialogBoxBinding.discardBtn.setOnClickListener {
                    discardDialogBox.dismiss()
                    sellFragment.discardAdData()
                    when (fragment) {
                        homeFragment -> {
                            binding.bottomNavView.selectedItemId = R.id.home
                        }

                        chatsFragment -> {
                            binding.bottomNavView.selectedItemId = R.id.chats
                        }

                        myAdsFragment -> {
                            binding.bottomNavView.selectedItemId = R.id.myads
                        }

                        accountFragment -> {
                            binding.bottomNavView.selectedItemId = R.id.account
                        }
                    }
                }
                dialogBoxBinding.cancelBtn.setOnClickListener {
                    discardDialogBox.dismiss()
                }


            } else {
                childFragmentManager.beginTransaction().hide(currentFragment).show(fragment)
                    .commit()
                currentFragment = fragment
                Log.d("uploadissue", "Fragment Changed")

            }

        } catch (_: java.lang.Exception) {
        } catch (_: Exception) {
        }

    }

    fun changeDot(dotShow: ImageView, dotHide: ImageView, dotHidee: ImageView) {
        dotShow.background = ContextCompat.getDrawable(requireContext(), R.drawable.circle_bg)
        dotHide.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_outline_bg)
        dotHidee.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.circle_outline_bg)
    }
    fun dotsWorking(currentPosition : Int){
        when (currentPosition) {
            0 -> {
                changeDot(binding.dot1, binding.dot2, binding.dot3)
            }

            1 -> {
                changeDot(binding.dot2, binding.dot1, binding.dot3)
            }

            2 -> {
                changeDot(binding.dot3, binding.dot1, binding.dot2)
            }
        }
    }
}
