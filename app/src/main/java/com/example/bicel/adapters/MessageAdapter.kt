package com.example.bicel.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.bicel.dataClasses.Message
import com.example.bicel.databinding.ActivityChatBinding
import com.example.bicel.databinding.ReceiveMessageBinding
import com.example.bicel.databinding.SendMessageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val messageList = ArrayList<Message>()
    val ITEM_RECEIVED = 1
    val ITEM_SENT = 2
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            return receiveViewHolder(ReceiveMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }else{
            return sendViewHolder(SendMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessageId = messageList[position].senderId
        if (Firebase.auth.currentUser?.uid.equals(currentMessageId)){
            return ITEM_SENT
        }else{
            return ITEM_RECEIVED
        }
    }
    override fun getItemCount(): Int {
        return messageList.size
    }
    fun setMessageList(list : ArrayList<Message>){
        messageList.clear()
        messageList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.javaClass == sendViewHolder::class.java){
            // Send ViewHolder
            val viewHolder = holder as sendViewHolder
            viewHolder.bind(message = messageList[position].message!!)
        }else{
            // Receive ViewHolder
            val viewHolder = holder as receiveViewHolder
            viewHolder.bind(message = messageList[position].message!!)
        }
    }
    inner class sendViewHolder(val binding: SendMessageBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(message: String){
            binding.messageTv.text = message
        }
    }
   inner class receiveViewHolder(val binding: ReceiveMessageBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(message: String){
            binding.messageTv.text = message
        }
    }
}