package com.example.bicel.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.databinding.AdViewpager4Binding
import com.squareup.picasso.Picasso

class AdDetailsViewPager : RecyclerView.Adapter<AdDetailsViewPager.adDetailsViewPagerHolder>()
{
    private val imageUris = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adDetailsViewPagerHolder {
        return adDetailsViewPagerHolder(AdViewpager4Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  imageUris.size
    }
    fun setList(list: ArrayList<String>){
        imageUris.clear()
        imageUris.addAll(list)
        notifyDataSetChanged()

    }
    override fun onBindViewHolder(holder: adDetailsViewPagerHolder, position: Int) {
        val imageUri = imageUris[position]
        Glide.with(holder.itemView.context).load(imageUri).into(holder.adImage)
    }
    inner class adDetailsViewPagerHolder(private val binding: AdViewpager4Binding) :
        RecyclerView.ViewHolder(binding.root) {
//        fun bind(imageUri: String) {
//        }
        val adImage = binding.adImage

    }


}