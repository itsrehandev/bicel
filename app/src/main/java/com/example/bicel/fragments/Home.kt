package com.example.bicel.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EdgeEffect
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bicel.AdDetailsActivity
import com.example.bicel.AllAds
import com.example.bicel.MainActivity
import com.example.bicel.R
import com.example.bicel.SharedPreferences.SharedPreferencesClass
import com.example.bicel.adapters.CarCatAdapter2
import com.example.bicel.adapters.HouseCatAdapter2
import com.example.bicel.adapters.MobCatAdapter2
import com.example.bicel.adapters.MotorCatAdapter2
import com.example.bicel.adapters.PcCatAdapter2
import com.example.bicel.adapters.categoriesAdapter
import com.example.bicel.adapters.recentSearchAdapter
import com.example.bicel.dataClasses.Ad
import com.example.bicel.databinding.FragmentHomeBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Home : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var SharedPreferences: SharedPreferencesClass
    private lateinit var firestore: FirebaseFirestore
    private var isContentLoaded = false

    // Recycler Views
    private lateinit var recentSearchRecyclerView: RecyclerView
    private lateinit var categoriesRv: RecyclerView
    private lateinit var mobRv: RecyclerView
    private lateinit var carRv: RecyclerView
    private lateinit var motorRv: RecyclerView
    private lateinit var houseRv: RecyclerView
    private lateinit var pcRv: RecyclerView

    //    Adapters
    private lateinit var recentSearchAdapter: recentSearchAdapter
    private lateinit var categoriesAdapter: categoriesAdapter
    private lateinit var mobCatAdapter: MobCatAdapter2
    private lateinit var carCatAdapter: CarCatAdapter2
    private lateinit var motorCatAdapter: MotorCatAdapter2
    private lateinit var houseCatAdapter: HouseCatAdapter2
    private lateinit var pcCatAdapter: PcCatAdapter2
    private val imageUrls = ArrayList<String>()
    lateinit var Recentlist: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Varaibles
        firestore = Firebase.firestore
        SharedPreferences = SharedPreferencesClass(requireContext())
        // Ends Here
        // Shimmer Effect
        binding.shimmer.startShimmer()
        binding.homeScroll.requestDisallowInterceptTouchEvent(true)
//        RecyclerViews Initialization
        mobRv = binding.mobCatRv
        carRv = binding.carCatRv
        motorRv = binding.motorCatRv
        houseRv = binding.houseCatRv
        pcRv = binding.pcCatRv
        recentSearchRecyclerView = binding.recentSearchRv
        categoriesRv = binding.categoriesRv
        // Common properties of Recycler Views
        val categoryRvs =
            listOf(mobRv, carRv, motorRv, houseRv, pcRv, recentSearchRecyclerView, categoriesRv)
        categoryRvs.forEach { recyclerView ->
            recyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                changeOverScrollColor(recyclerView)
        }
        //Adapters Initialization
        mobCatAdapter = MobCatAdapter2(this)
        carCatAdapter = CarCatAdapter2(this)
        motorCatAdapter = MotorCatAdapter2(this)
        houseCatAdapter = HouseCatAdapter2(this)
        pcCatAdapter = PcCatAdapter2(this)
        recentSearchAdapter = recentSearchAdapter()
        categoriesAdapter = categoriesAdapter()
        // Adapter Assignment
        mobRv.adapter = mobCatAdapter
        carRv.adapter = carCatAdapter
        motorRv.adapter = motorCatAdapter
        houseRv.adapter = houseCatAdapter
        pcRv.adapter = pcCatAdapter
        recentSearchRecyclerView.adapter = recentSearchAdapter
        categoriesRv.adapter = categoriesAdapter

        Recentlist = arrayListOf(
            "core i5 4th generation computer",
            "5 marla house for sale in wah cantt",
            "Shopping trolley",
            "Honda Cd 70 2021 model ",
            "Double bed stylish"
        )
        recentSearchAdapter.setSearchList(Recentlist)
        // Catrgories Adapter
        binding.swipeRefreshLayout.setProgressViewOffset(true, 150, 350)
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchAllCategoriesData()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(400)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        fetchImagesFromStorage()
        fetchAllCategoriesData()
        Thread {
            while (!isContentLoaded) {
                Thread.sleep(10)
            }
            if(isAdded){
            requireActivity().runOnUiThread {
                binding.shimmerLayout.visibility = View.GONE
                binding.shimmer.stopShimmer()
            }
            }
        }.start()
        val seeAllTvs = arrayListOf(binding.seeAllCar, binding.seeAllMotor, binding.seeAllHouse, binding.SeeAllPc)
        seeAllTvs.forEach {
            it.setOnClickListener {
               startActivity(Intent(requireContext(),AllAds::class.java))
            }
        }
    }

    private fun fetchImagesFromStorage() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        // Replace with your actual image paths
        val imagesRef = storageRef.child("CategoryImages/HomeBrowse")

        imagesRef.listAll()
            .addOnSuccessListener { listResult ->
                val downloadTasks = listResult.items.map { item ->
                    item.downloadUrl.continueWith { task ->
                        if (task.isSuccessful) {
                            task.result.toString()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                task.exception?.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Tasks.whenAllComplete(downloadTasks).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val urls = task.result?.mapNotNull { it.result as? String } ?: emptyList()
                        // Sort the URLs alphabetically
                        val sortedUrls = urls.sorted()
                        // Update the RecyclerView with the sorted list
                        imageUrls.clear()
                        imageUrls.addAll(sortedUrls)
                        categoriesAdapter.setList(imageUrls)
                    } else {
                        // Handle errors
                        task.exception?.let { e ->
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun fetchAdData(category: String) {
        firestore.collection("userAds").whereEqualTo("category", category)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(5)
            .get().addOnSuccessListener { querySnapshot ->
                val adsList = ArrayList<Ad>()
                for (document in querySnapshot.documents) {
                    val ad = document.toObject(Ad::class.java)
                    ad?.let {
                        adsList.add(it)
                    }

                }
                if (adsList.isEmpty()){
                    contentLoaded()
                }
                when (category) {
                    "Mobiles" -> {
                        if (adsList.isEmpty()) {
                            binding.noMobileAdsTv.visibility = View.VISIBLE
                        } else {
                            binding.noMobileAdsTv.visibility = View.GONE
                        }
                        mobCatAdapter.setAdsList(adsList)
                    }

                    "Cars" -> {
                        if (adsList.isEmpty()) {
                            binding.noCarAdsTv.visibility = View.VISIBLE
                        } else {
                            binding.noCarAdsTv.visibility = View.GONE
                        }
                        carCatAdapter.setAdsList(adsList)
                    }

                    "Motorcycles" -> {
                        if (adsList.isEmpty()) {
                            binding.noMotorAdsTv.visibility = View.VISIBLE
                        } else {
                            binding.noMotorAdsTv.visibility = View.GONE
                        }
                        motorCatAdapter.setAdsList(adsList)
                    }

                    "Houses" -> {
                        if (adsList.isEmpty()) {
                            binding.noHouseAdsTv.visibility = View.VISIBLE
                        } else {
                            binding.noHouseAdsTv.visibility = View.GONE
                        }
                        houseCatAdapter.setAdsList(adsList)
                    }

                    "Computers" -> {
                        if (adsList.isEmpty()) {
                            binding.noPcAdsTv.visibility = View.VISIBLE
                        } else {
                            binding.noPcAdsTv.visibility = View.GONE
                        }
                        pcCatAdapter.setAdsList(adsList)
                    }
                }


            }.addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
            }
    }

    fun changeOverScrollColor(recyclerView: RecyclerView) {
        recyclerView.edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
            override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                val edgeEffect = EdgeEffect(recyclerView.context)
                edgeEffect.setColor(
                    ContextCompat.getColor(
                        recyclerView.context,
                        R.color.colorPrimary
                    )
                )
                return edgeEffect
            }
        }
    }

    fun showAdDetails(adData: Bundle) {
        startActivity(
            Intent(requireContext(), AdDetailsActivity::class.java).putExtra(
                "adData",
                adData
            )
        )

    }

    fun fetchAllCategoriesData() {
        fetchAdData("Mobiles")
        fetchAdData("Cars")
        fetchAdData("Motorcycles")
        fetchAdData("Houses")
        fetchAdData("Computers")

    }

    fun contentLoaded() {
        isContentLoaded = true
    }

}