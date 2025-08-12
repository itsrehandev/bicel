package com.example.bicel.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bicel.AdDetailsActivity
import com.example.bicel.R
import com.example.bicel.adapters.MyAdsAdapter
import com.example.bicel.dataClasses.Ad
import com.example.bicel.databinding.FragmentMyAdsBinding
import com.example.bicel.main
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MyAds : Fragment() {
    private lateinit var binding: FragmentMyAdsBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var auth: FirebaseAuth
    private lateinit var myAdsRv: RecyclerView
    private lateinit var myAdsAdapter: MyAdsAdapter
    private lateinit var myAdsList: ArrayList<Ad>
    private lateinit var currentAdsList: ArrayList<Ad>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAdsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialization
        firestore = Firebase.firestore
        firebaseStorage = Firebase.storage
        firebaseAuth = Firebase.auth
        myAdsRv = binding.myAdsRv
        myAdsAdapter = MyAdsAdapter(this)
        myAdsRv.adapter = myAdsAdapter
        auth = Firebase.auth
        myAdsList = ArrayList()
        currentAdsList = ArrayList()
        Log.d("loggingIssue", "onViewCreated: " + firebaseAuth.currentUser?.uid)
        val mainFrag = parentFragment as main
        //

        mainFrag.userViewModel.isLoggedIn.observe(viewLifecycleOwner){isLoggedIn->
            if(isLoggedIn || firebaseAuth.currentUser != null){
                binding.noLoginScreen.visibility = View.GONE
                binding.myAdsRv.visibility = View.VISIBLE
                binding.header.visibility = View.VISIBLE
                fetchMyAds()
            }else{
                binding.noLoginScreen.visibility = View.VISIBLE
                binding.myAdsRv.visibility = View.GONE
                binding.noAdsCont.visibility = View.GONE
                binding.header.visibility = View.GONE
            }
        }
        myAdsRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        // When no ad is present, upload button functionality
        binding.uploadBtn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                delay(250)
                mainFrag.binding.bottomNavView.selectedItemId = R.id.sell
            }
        }
        // Keep checking if a new ad is uploaded, Refresh UI
        mainFrag.userViewModel.isAdUploaded.observe(viewLifecycleOwner) { isAdUploaded ->
            if (isAdUploaded) {
                fetchMyAds()
                mainFrag.userViewModel.isAdUploaded.value = false
            }
        }
//        Filter Spinner Funtionality
        val filters = arrayOf("Latest", "Newest", "Price High to Low", "Price Low to High")
        val filterAdapter = ArrayAdapter(requireContext(), R.layout.category_spinner_4, filters)
        binding.filterSpinner.adapter = filterAdapter
        binding.filterSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val latestAds = myAdsList.sortedByDescending { it.timeStamp!!.seconds.toInt() }
                    val newestAds = myAdsList.sortedBy { it.timeStamp!!.seconds.toInt() }
                    val htlAds =
                        myAdsList.sortedByDescending { it.price.toInt() } // Price high to low
                    val lthAds =
                        myAdsList.sortedBy { it.price.toInt() } // Price low to high ads list(Ascending order)
                    when (position) {

                        0 -> {
                            currentAdsList.clear()
                            currentAdsList.addAll(latestAds)
                            myAdsAdapter.setAdsList(currentAdsList)
                        }

                        1 -> {
                            currentAdsList.clear()
                            currentAdsList.addAll(newestAds)
                            myAdsAdapter.setAdsList(currentAdsList)
                        }

                        2 -> {
                            currentAdsList.clear()
                            currentAdsList.addAll(htlAds)
                            myAdsAdapter.setAdsList(currentAdsList)
                        }

                        3 -> {
                            currentAdsList.clear()
                            currentAdsList.addAll(lthAds)
                            myAdsAdapter.setAdsList(currentAdsList)
                        }


                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    return
                }

            }
        // Filter Btn
        binding.filterBtn.setOnClickListener {
                binding.filterSpinner.performClick()

        }
    }
//      Functions
    fun fetchMyAds() {
        myAdsList.clear()
        firestore.collection("userAds").whereEqualTo("uploaderId", firebaseAuth.uid).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val myAd = document.toObject(Ad::class.java)
                    if (myAd != null) {
                        myAdsList.add(myAd)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG).show()
            }.addOnCompleteListener {

                if (myAdsList.isEmpty() && firebaseAuth.currentUser?.uid != null) {
                    binding.noAdsCont.visibility = View.VISIBLE
                    binding.myAdsRv.visibility = View.GONE
                } else {
                    myAdsAdapter.setAdsList(myAdsList)
                    binding.noAdsCont.visibility = View.GONE
                    binding.myAdsRv.visibility = View.VISIBLE
                }
            }
    }

    fun deleteAd(timeStamp: Timestamp,imagesUri : ArrayList<String>) {
        firestore.collection("userAds")
            .whereEqualTo("uploaderId", firebaseAuth.currentUser?.uid)
            .whereEqualTo("timeStamp", timeStamp).get().addOnSuccessListener {
                it.documents[0].reference.delete().addOnSuccessListener {
                    Toast.makeText(requireContext(), "Ad deleted successfully", Toast.LENGTH_LONG)
                        .show()
                    fetchMyAds()
                }
            }.addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Unfortunately, Some error occurred",
                    Toast.LENGTH_LONG
                ).show()
            }
        imagesUri.forEach {
            firebaseStorage.getReferenceFromUrl(it).delete().addOnSuccessListener {
                Log.d("deleted","deleted" + it)
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
}