package com.example.bicel.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.R
import com.example.bicel.databinding.StartupRvBinding

class StartupImgAdapter : RecyclerView.Adapter<StartupImgAdapter.MyViewHolder>()
{
    val imgs = ArrayList<Int>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(StartupRvBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  imgs.size
    }
    fun setImgs(list: List<Int>) {
        imgs.clear()
        imgs.addAll(list)
        notifyDataSetChanged()
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentImg = imgs[position]
        holder.bind(currentImg)

    }
    inner class MyViewHolder(private val binding: StartupRvBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(currentSearch: Int){
            Glide.with(binding.root.context).load(currentSearch).into(binding.img)
        }
    }
}