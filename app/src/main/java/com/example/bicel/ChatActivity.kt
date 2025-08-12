package com.example.bicel

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bicel.adapters.MessageAdapter
import com.example.bicel.dataClasses.Message
import com.example.bicel.databinding.ActivityChatBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    // Variables Declaration
    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var receiverUid: String
    private lateinit var senderUid: String
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private lateinit var messageList: ArrayList<Message>
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messagesRv: RecyclerView
    private lateinit var databaseRef: DatabaseReference

    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //        Min height of main layout
//        binding.main.minHeight = resources.displayMetrics.heightPixels
        // Variables Initialization
        firebaseAuth = Firebase.auth
        receiverUid = intent.getStringExtra("receiverUid").toString()
        senderUid = firebaseAuth.currentUser?.uid!!
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        messageList = ArrayList()
        messagesRv = binding.messagesRv
        messageAdapter = MessageAdapter()
        databaseRef = FirebaseDatabase.getInstance().getReference()
        firestore = FirebaseFirestore.getInstance()
        // Ends
        messagesRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        messagesRv.adapter = messageAdapter
        binding.messagesRv.scrollToPosition(messageAdapter.itemCount - 1)
        // Back Btn at Top Bar
        binding.backBtn.setOnClickListener {
            finish()
        }

        val ReceiverName = intent.getStringExtra("userName")
        val ReceiverprofilePicUri = intent.getStringExtra("profilePicUri")
        // Setting user name and picture on top bar
        if (intent.hasExtra("userName") && intent.hasExtra("profilePicUri")) {
            binding.receiverName.text = ReceiverName
            Glide.with(this).asBitmap().load(ReceiverprofilePicUri).into(binding.receiverImg)
            lifecycleScope.launch(Dispatchers.Main) {
                delay(1000)
                binding.messagesProgress.visibility = View.GONE

            }
        }
        // Sending Message Button
        binding.sendMessage.setOnClickListener {
            sendMessage()
        }
        Log.d("dbref", senderRoom.toString())
        databaseRef.child("chats").child(senderRoom!!).child("messages").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (message in snapshot.children) {
                        val messageObject = message.getValue(Message::class.java)
                        messageList.add(messageObject!!)
                        messageAdapter.setMessageList(messageList)
                        binding.messagesRv.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                    messageList.clear()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            }
        )
    }

    fun sendMessage() {
        if (senderUid != receiverUid) {
            val message = binding.messageEt.text.toString()
            val messageObject = Message(message, senderUid)
            Log.d("dbref", databaseRef.toString())
            Log.d("dbref", messageObject.toString())
            databaseRef.child("chats").child(senderRoom!!).child("messages").push()
                .setValue(messageObject).addOnSuccessListener {
                    binding.messagesRv.scrollToPosition(messageAdapter.itemCount - 1)
                    databaseRef.child("chats").child(receiverRoom!!).child("messages").push()
                        .setValue(messageObject)

                    binding.messageEt.setText("")
//                    Add to Contact List
                    val buyingMap = mapOf(
                        "contact" to receiverUid,
                        "type" to "buyer"
                    )
                    if (receiverUid.isNotEmpty()) {
                        firestore.collection("users").document(firebaseAuth.currentUser!!.uid).get()
                            .addOnSuccessListener {
                                val contactList =
                                    it.get("contactList") as ArrayList<Map<String, String>>
                                contactList.forEach { contactMap ->
                                    if (contactMap.isNotEmpty()) {
                                        if (contactMap.get("contact") == receiverUid) {
                                            // Don't add to contacts ~ Already added
                                            return@addOnSuccessListener
                                        }

                                    }
                                }
                                // Not already added
                                firestore.collection("users")
                                    .document(firebaseAuth.currentUser!!.uid)
                                    .update("contactList", FieldValue.arrayUnion(buyingMap))
                                    .addOnSuccessListener {// Contact Updated
                                        Toast.makeText(this, "Sender Contact Updated", Toast.LENGTH_SHORT).show()
                                    }
                        firestore.collection("users").document(receiverUid).get().addOnSuccessListener {
                            val receiverContactList = it.get("contactList")  as ArrayList<Map<String, String>>
                            receiverContactList.forEach{ contactMap->
                                if (contactMap.isNotEmpty()){
                                    if(contactMap.get("contact") == senderUid){
                                        // Don't add to contacts ~ Already added
                                        return@addOnSuccessListener
                                    }

                                }
                            }
                            // Not already added ~ Add
                            val sellingMap = mapOf(
                                "contact" to senderUid,
                                "type" to "seller"
                            )
                            firestore.collection("users").document(receiverUid).update("contactList", FieldValue.arrayUnion(sellingMap))
                                .addOnSuccessListener { // Contact Added to Receiver contactlist
                                    Toast.makeText(this, "Receiver Contact Updated", Toast.LENGTH_SHORT).show()
                                }

                        }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send message ${it}", Toast.LENGTH_SHORT).show()
                }
        }
            else {
                Toast.makeText(this, "You cannot send message to yourself", Toast.LENGTH_SHORT)
                    .show()
            }

    }
}