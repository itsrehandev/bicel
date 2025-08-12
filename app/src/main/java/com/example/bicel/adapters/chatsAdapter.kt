package com.example.bicel.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.ChatActivity
import com.example.bicel.R
import com.example.bicel.dataClasses.ChatUser
import com.example.bicel.databinding.ChatRowBinding
import com.example.bicel.databinding.FragmentChatsBinding
import com.example.bicel.fragments.Chats

class chatsAdapter(val chatFrag: Chats) : RecyclerView.Adapter<chatsAdapter.chatViewHolder>() {

    private var chatUsers = ArrayList<ChatUser>()
  var Uid:String =""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): chatViewHolder {
        return chatViewHolder(
            ChatRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return chatUsers.size
    }
fun setList(list : ArrayList<ChatUser>){
    chatUsers.clear()
    chatUsers.addAll(list)
    notifyDataSetChanged()
}


    override fun onBindViewHolder(holder: chatViewHolder, position: Int) {
        holder.bind(
            userName = chatUsers[position].name,
            profilePicUri = chatUsers[position].profileUri,
            lastMessage = chatUsers[position].lastMessage,
            receiverUid = chatUsers[position].uid

        )

    }

    inner class chatViewHolder(val binding: ChatRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(userName: String, profilePicUri: String, lastMessage: String, receiverUid: String) {
            binding.userName.text = userName
            binding.lastMsg.text = lastMessage
            Glide.with(itemView.context).asBitmap()
                .load(profilePicUri).into(binding.userImg)
            binding.root.setOnClickListener{
                if (chatFrag.contactsSelected()>0
                    // some contact is selected
                    ){
                    // check if user is clicking on selected Contact
                    if((binding.root.background as ColorDrawable).color == ContextCompat.getColor(itemView.context,R.color.colorPrimaryOverlay)) {
                    binding.root.background = ContextCompat.getDrawable(itemView.context,R.color.white)
                        Uid = receiverUid
                    chatFrag.unselectContact()
                    }
                    // if user is clicking on unselected contact
                    else{// select the contact
                        Uid = receiverUid
                        binding.root.background = ContextCompat.getDrawable(itemView.context,R.color.colorPrimaryOverlay)
                        chatFrag.updateSelectedContactNumber()

                    }
                    return@setOnClickListener
                }else{

                itemView.context.startActivity(Intent(itemView.context, ChatActivity::class.java)
                    .putExtra("userName",userName)
                    .putExtra("profilePicUri",profilePicUri)
                    .putExtra("receiverUid",receiverUid)
                )

                }
            }
            binding.root.setOnLongClickListener {
                Uid = receiverUid
                binding.root.background = ContextCompat.getDrawable(itemView.context,R.color.colorPrimaryOverlay)
                chatFrag.showContactOptionsTable()
                true
            }
        }
    }
    fun selectedUid() : String{
        return Uid
    }
}