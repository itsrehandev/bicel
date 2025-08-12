package com.example.bicel.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.dataClasses.Ad
import com.example.bicel.databinding.CatergoryAdCard2Binding
import com.example.bicel.fragments.Home
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MobCatAdapter2(private val homeFrag: Home) :
    RecyclerView.Adapter<MobCatAdapter2.myViewHolder>() {

    private val adsList = ArrayList<Ad>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): myViewHolder {
        return myViewHolder(
            CatergoryAdCard2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun getItemCount(): Int {
        return adsList.size
    }

    fun setAdsList(list: ArrayList<Ad>) {
        adsList.clear()
        adsList.addAll(list)
        notifyDataSetChanged()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: myViewHolder, position: Int) {
        val adImageUrl = adsList[position].ImagesUrl[0]
        Glide.with(holder.itemView.context).asBitmap()
            .load(adImageUrl).into(holder.adImage)
        val uploadingTime = adsList[position].timeStamp!!.toDate().time
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
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - diffMillis))
            }
        }
        holder.bindData(
            adPrice = adsList[position].price,
            adTitle = adsList[position].title,
            adLocation = "Ahmadabad , Rawalpindi, Punjab ",
            adTime = getTimeAgo(timeDifference),
            adDescription = adsList[position].description,
            adImagesList = adsList[position].ImagesUrl,
            adCategory = adsList[position].category,
            adUploader = adsList[position].uploaderId
        )
    }

    inner class myViewHolder(val binding: CatergoryAdCard2Binding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(
            adPrice: String,
            adTitle: String,
            adLocation: String,
            adTime: String,
            adDescription: String,
            adImagesList: ArrayList<String>,
            adCategory: String,
            adUploader: String
        ) {
            binding.adPrice.text = NumberFormat.getIntegerInstance(Locale.US).format(adPrice.toInt())
            binding.adTitle.text = adTitle
            binding.adLocation.text = adLocation
            binding.adTime.text = adTime

            binding.root.setOnClickListener {
                val bundle = bundleOf(
                    Pair("adPrice", adPrice),
                    Pair("adTitle", adTitle),
                    Pair("adDescription", adDescription),
                    Pair("adImages", adImagesList),
                    Pair("adCategory", adCategory),
                    Pair("adUploader", adUploader)
                )
                homeFrag.showAdDetails(adData = bundle)
            }

        }


        val adImage = binding.adImage
    }

}