package com.example.bicel.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.R
import com.example.bicel.dataClasses.Ad
import com.example.bicel.databinding.DiscardAdDialogBinding
import com.example.bicel.databinding.MyAdsBinding
import com.example.bicel.fragments.MyAds
import com.example.bicel.main
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyAdsAdapter(private val myAdsFrag: MyAds) : RecyclerView.Adapter<MyAdsAdapter.myAdsVH>() {
    private var myAdsList = ArrayList<Ad>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myAdsVH {
        return myAdsVH(MyAdsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return myAdsList.size
    }

    fun setAdsList(list: ArrayList<Ad>) {
        myAdsList.clear()
        myAdsList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: myAdsVH, position: Int) {
        val adImageUrl = myAdsList[position].ImagesUrl[0]
        Glide.with(holder.itemView.context)
            .load(adImageUrl).into(holder.adImage)
        val uploadingTime = myAdsList[position].timeStamp!!.toDate().time
        val currentTime = Date().time
        val timeDifference = currentTime - uploadingTime
        fun getTimeAgo(diffMillis: Long): String {
            val seconds = diffMillis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24

            return when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minute${if (minutes != 1L) "s" else ""} ago"
                hours < 24 -> "$hours hour${if (hours != 1L) "s" else ""} ago"
                days < 7 -> "$days day${if (days != 1L) "s" else ""} ago"
                days < 14 -> "1 Week ago"
                days < 21 -> "2 Weeks ago"
                days < 28 -> "3 Weeks ago"
                days < 56 -> "1 Month ago"
                days < 84 -> "2 Month ago"
                days < 112 -> "3 Month ago"
                else -> SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(System.currentTimeMillis() - diffMillis))
            }
        }
        holder.bindData(
            adPrice = myAdsList[position].price,
            adTitle = myAdsList[position].title,
            adLocation = "Ahmadabad , Rawalpindi, Punjab ",
            adTime = getTimeAgo(timeDifference),
            adDescription = myAdsList[position].description,
            adImagesList = myAdsList[position].ImagesUrl,
            adCategory = myAdsList[position].category,
            adUploader = myAdsList[position].uploaderId,
            timeStamp = myAdsList[position].timeStamp!!
        )
    }

    inner class myAdsVH(val binding: MyAdsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            adPrice: String,
            adTitle: String,
            adLocation: String,
            adTime: String,
            adDescription: String,
            adImagesList: ArrayList<String>,
            adCategory: String,
            adUploader: String,
            timeStamp: Timestamp
        ) {

            binding.adPrice.text =
                NumberFormat.getIntegerInstance(Locale.US).format(adPrice.toInt())
            Log.d("myadserros", "4..... Success, ${binding.adPrice.text.toString()},,, ")
            binding.adTitle.text = adTitle
            binding.adLocation.text = adLocation
            binding.adTime.text = adTime
            binding.deleteBtn.setOnClickListener {
                val deleteAdBinding: DiscardAdDialogBinding =
                    DiscardAdDialogBinding.inflate(LayoutInflater.from(myAdsFrag.context))
                val builder = androidx.appcompat.app.AlertDialog.Builder(
                    myAdsFrag.requireContext(),
                    R.style.CustomAlertDialog
                ).setView(deleteAdBinding.root).create()
                deleteAdBinding.message.text = "Do you really want to delete this ad?"
                deleteAdBinding.discardBtn.text = "Delete"
                deleteAdBinding.cancelBtn.setOnClickListener {
                    builder.dismiss()
                }
                deleteAdBinding.discardBtn.setOnClickListener {
                    myAdsFrag.deleteAd(timeStamp,adImagesList)
                    builder.dismiss()
                }
                builder.show()
            }
                val bundle = bundleOf(
                    Pair("adPrice", adPrice),
                    Pair("adTitle", adTitle),
                    Pair("adDescription", adDescription),
                    Pair("adImages", adImagesList),
                    Pair("adCategory", adCategory),
                    Pair("adUploader", adUploader),
                    Pair("adTime", timeStamp)
                )
            binding.root.setOnClickListener {
                myAdsFrag.showAdDetails(adData = bundle)
            }
            binding.editBtn.setOnClickListener {
                val mainFrag = myAdsFrag.parentFragment as main
                mainFrag.binding.bottomNavView.selectedItemId = R.id.sell
                mainFrag.sellFragment.insertAdData(bundle)
            }
        }
        val adImage = binding.adImage
    }
}