package com.example.bicel.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.databinding.CategoriesItem2Binding


class categoriesAdapter : RecyclerView.Adapter<categoriesAdapter.categoriesViewHolder>()
{
    private val imageUrls = ArrayList<String>()
    private val categoryTitle = arrayListOf<String>("Bikes","Cars","Cloths","Computer","Furniture","Grocery","Home decoration","Mobiles","Pets","Sports")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): categoriesViewHolder {
        return categoriesViewHolder(CategoriesItem2Binding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return  imageUrls.size
    }
fun setList(list: ArrayList<String>){
    imageUrls.clear()
    imageUrls.addAll(list)
    notifyDataSetChanged()
}
    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: categoriesViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)
        holder.categoryName.text = categoryTitle[position]

    }
    inner class categoriesViewHolder(private val binding: CategoriesItem2Binding) :
        RecyclerView.ViewHolder(binding.root) {
    val imageView: ImageView = binding.categoryImg
        val categoryName = binding.categoryTv
        }


}