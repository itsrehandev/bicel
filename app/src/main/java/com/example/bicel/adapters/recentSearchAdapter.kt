package com.example.bicel.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bicel.R
import com.example.bicel.databinding.RecentSearch2Binding

class recentSearchAdapter : RecyclerView.Adapter<recentSearchAdapter.MyViewHolder>()
{
    val searchList = ArrayList<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(RecentSearch2Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  searchList.size
    }
    fun setSearchList(list: List<String>) {
        searchList.clear()
        searchList.addAll(list)
        notifyDataSetChanged()
    }
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentSearch = searchList[position]
        holder.bind(currentSearch)

    }
    inner class MyViewHolder(private val binding: RecentSearch2Binding) : RecyclerView.ViewHolder(binding.root){
        fun bind(currentSearch: String){
            binding.recentSearchTv.text = currentSearch
        }
    }
}