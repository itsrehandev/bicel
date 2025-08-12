package com.example.bicel.adapters


import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.databinding.AdViewpager4Binding
import com.example.bicel.databinding.FragmentSellBinding
import com.example.bicel.fragments.Sell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class adViewPager(val sellFrag : Sell) : RecyclerView.Adapter<adViewPager.adViewPagerHolder>()
{
    private val imageUris = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adViewPagerHolder {
        return adViewPagerHolder(AdViewpager4Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  imageUris.size
    }
    fun setList(list: ArrayList<String>){
        imageUris.clear()
        imageUris.addAll(list)
        notifyDataSetChanged()

    }
    override fun onBindViewHolder(holder: adViewPagerHolder, position: Int) {
        val imageUri = imageUris[position]
        holder.bind(imageUri)
        sellFrag.deleteAdImg(position)
    }
    inner class adViewPagerHolder(private val binding: AdViewpager4Binding) :
        RecyclerView.ViewHolder(binding.root) {
             fun bind(imageUri: String) {
                Glide.with(binding.adImage.context)
                    .load(imageUri)
                    .into(binding.adImage)
            }

    }
}