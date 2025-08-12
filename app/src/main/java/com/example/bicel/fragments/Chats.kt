package com.example.bicel.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bicel.R
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.adapters.chatsAdapter
import com.example.bicel.dataClasses.ChatUser
import com.example.bicel.databinding.DiscardAdDialogBinding
import com.example.bicel.databinding.FragmentChatsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Chats : Fragment() {
    private lateinit var binding: FragmentChatsBinding
    private lateinit var chatsAdapter: chatsAdapter
    private lateinit var chatsRv: RecyclerView
    private var chatsUserList = ArrayList<ChatUser>()
    private var typeChatsList = ArrayList<ChatUser>()
    private var currentChatsList = ArrayList<ChatUser>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
     lateinit var userViewModel: UserViewModel
    private var chatUserId: String? = null
    private var contactsType = "all"
    private var contactsSelected = 0
    private lateinit var contactList: ArrayList<Map<String, String>>
    private var selectedContactList: ArrayList<String> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Variables
        chatsAdapter = chatsAdapter(this)
        chatsRv = binding.chatUsersRv
        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = Firebase.auth
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        chatUserId = activity?.intent?.getBundleExtra("chatBundle")?.getString("uploaderId")
        // Chats Adapter
        userViewModel.isLoggedIn.observe(viewLifecycleOwner){isLoggedIn->
            if(isLoggedIn || firebaseAuth.currentUser != null){
                fetchContacts()
                binding.noChatsTv.visibility = View.GONE
                binding.chatUsersRv.visibility = View.VISIBLE
                binding.noChatsTv.setText("Your chats will appear here. Currently you don't have any")
            }else{
                binding.noChatsTv.visibility = View.VISIBLE
                binding.chatUsersRv.visibility = View.GONE
                binding.noChatsTv.setText("Once you Sign up, Your Chats will appear here.")
            }
        }
        chatsRv.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        chatsRv.adapter = chatsAdapter
        binding.chipAll.isChecked = true

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.first()) {
                binding.chipAll.id -> {
                    contactsType = "all"
                    filterContactType()
                    searchChats()
                }

                binding.chipBuying.id -> {
                    contactsType = "buyer"
                    filterContactType()
                    searchChats()
                }

                binding.chipSelling.id -> {
                    contactsType = "seller"
                    filterContactType()
                    searchChats()
                }
            }
        }
        binding.cancelContactOpt.setOnClickListener {
            cancelSelectedContacts()
        }
        binding.deleteContact.setOnClickListener {
            showDeleteDialog()
        }
        searchFuntionality()
    }


//    fun UpdateAndGetContactList() {
//        activity.let {
//
//
//            Log.d("firebaseError", "chat : $chatUserId")
//            val buyingMap = mapOf(
//                "contact" to chatUserId,
//                "type" to "buyer"
//            )
//            if (chatUserId != null) {
//                firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
//                    .update("contactList", FieldValue.arrayUnion(buyingMap)).addOnSuccessListener {
//                        firestore.collection("users").document(firebaseAuth.currentUser!!.uid).get()
//                            .addOnSuccessListener {
//                                val contactList =
//                                    it.get("contactList") as ArrayList<Map<String, String>>
//                                contactList.forEach { contactMap ->
//
//                                    firestore.collection("users")
//                                        .document(contactMap.get("contact")!!)
//                                        .get()
//                                        .addOnSuccessListener {
//                                            val userName = it.getString("name")
//                                            val profilePicUri = it.getString("profilePicUri")
//
//                                            chatsUserList.add(
//                                                ChatUser(
//                                                    name = userName!!,
//                                                    profileUri = profilePicUri!!,
//                                                    lastMessage = "This is last Message",
//                                                    uid = contactMap.get("contact")!!
//                                                )
//                                            )
//
//                                        }
//                                        .addOnFailureListener {
//                                            Log.d("firebaseError", "Error3: ${it.message}")
//                                        }
//                                }
//                                lifecycleScope.launch(Dispatchers.Main) {
//                                    delay(500)
//                                    Log.d("firebaseError", "chatsUserList : $chatsUserList")
//                                    chatsAdapter.setList(chatsUserList)
//
//                                }
//                            }
//                            .addOnFailureListener {
//                                Log.d("firebaseError", "Error2: ${it.message}")
//                            }
//                    }
//                    .addOnFailureListener {
//                        Log.d("firebaseError", "Error1: ${it.message}")
//                    }
//            }
//        }
//    }

    fun fetchContacts() {
        firestore.collection("users").document(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                contactList = it.get("contactList") as ArrayList<Map<String, String>>
                if (contactList.isNotEmpty()) {

                    contactList.forEach { contactMap ->
                        val contactType = contactMap.get("type")!!
                            firestore.collection("users")
                                .document(contactMap.get("contact")!!)
                                .get()
                                .addOnSuccessListener {
                                    val userName = it.getString("name")
                                    val profilePicUri = it.getString("profilePicUri")

                                    chatsUserList.add(
                                        ChatUser(
                                            name = userName!!,
                                            profileUri = profilePicUri!!,
                                            lastMessage = "This is last Message",
                                            uid = contactMap.get("contact")!!,
                                            type = contactType
                                        )
                                    )

                                }
                        }
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(500)
                        chatsAdapter.setList(chatsUserList)
                    }
                    binding.noChatsTv.visibility = View.GONE
                    binding.chatUsersRv.visibility = View.VISIBLE
                } else {
                    binding.noChatsTv.visibility = View.VISIBLE
                    binding.chatUsersRv.visibility = View.GONE
                }


            }

    }


    fun updateSelectedContactNumber() {
        contactsSelected = contactsSelected + 1
        selectedContactList.add(chatsAdapter.selectedUid())
        binding.selectContactTv.text = contactsSelected.toString()
    }

    fun showContactOptionsTable() {
        updateSelectedContactNumber()
        binding.contactOptTable.visibility = View.VISIBLE
    }

    fun contactsSelected(): Int {
        return contactsSelected
    }

    fun unselectContact() {
            contactsSelected = contactsSelected - 1
            binding.selectContactTv.text = contactsSelected.toString()
            selectedContactList.remove(chatsAdapter.selectedUid())
        if(contactsSelected==0){
            cancelSelectedContacts()
        }

//        if (contactsSelected > 0) {
//        }
//        if (contactsSelected == 0) {
//            cancelSelectedContacts()
//            return false // _isAnyContactSelected
//        } else if (contactsSelected > 0) {  // in case of greater than 1
//            return true
//        }
//        return true // it will never happen
    }

    fun deleteChats() {

        Log.d("ListError", selectedContactList.toString())
        val updatedList = contactList.filter {
            it["contact"] !in selectedContactList
        }
        Log.d("ListError", updatedList.toString())
        firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
            .update("contactList", updatedList).addOnSuccessListener {
            Toast.makeText(requireContext(), "Contact lIst UpdAtED", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
        }
        cancelSelectedContacts()
    }

    fun showDeleteDialog() {
        val deleteDialogBinding: DiscardAdDialogBinding =
            DiscardAdDialogBinding.inflate(layoutInflater)
        val deleteDialog =
            androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(deleteDialogBinding.root).create()
        deleteDialogBinding.discardBtn.setOnClickListener {
            deleteChats()
            deleteDialog.dismiss()
        }
        deleteDialogBinding.message.height = context?.resources!!.getDimensionPixelSize(com.intuit.sdp.R.dimen._36sdp)
        deleteDialogBinding.message.text = "Do you want to delete selected chats?"
        deleteDialogBinding.discardBtn.text = "Delete"
        deleteDialog.show()
        deleteDialogBinding.cancelBtn.setOnClickListener {
            deleteDialog.dismiss()
        }

    }

    fun cancelSelectedContacts() {
        // Change highlight color of all the contacts
        for (i in 0 until chatsRv.childCount) {
            val viewHolder =
                chatsRv.findViewHolderForAdapterPosition(i) as? chatsAdapter.chatViewHolder
            viewHolder?.binding?.root?.background =
                ContextCompat.getDrawable(requireContext(), R.color.white)
        }
        //reset Selected Contacts Uids
        selectedContactList.clear()
        chatsAdapter.Uid = ""
        // reset the contact selected number and hide the table
        contactsSelected = 0
        binding.contactOptTable.visibility = View.GONE
    }
    fun filterContactType () {
        if (contactsType == "all") {
            chatsAdapter.setList(chatsUserList)
            typeChatsList.clear()
            typeChatsList.addAll(chatsUserList)
        } else {
            val filteredList = chatsUserList.filter {
                it.type == contactsType
            }
            typeChatsList.clear()
            typeChatsList.addAll(filteredList)
            chatsAdapter.setList(typeChatsList)
        }
    }
    fun searchFuntionality() {
        binding.searchView.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                if (!s.isNullOrEmpty()){ // if search view is not null
                    val searchedChats = typeChatsList.filter {
//                        Log.d("firebaseError", "it.name.contains(query) : ${it.name.contains(query)}" +"... ${it.name}")
                        it.name.lowercase().contains(query)
                    }
                    currentChatsList.clear()
                    currentChatsList.addAll(searchedChats)
                    chatsAdapter.setList(currentChatsList)
                }
                else{ // if search view is empty
                    chatsAdapter.setList(typeChatsList)
                }

            }

        })
    }
  fun  searchChats(){
        val query = binding.searchView.text.toString().lowercase()
      if(query.isNotEmpty()){
          val searchedChats = typeChatsList.filter {
              it.name.lowercase().contains(query)
          }
          currentChatsList.clear()
          currentChatsList.addAll(searchedChats)
          chatsAdapter.setList(currentChatsList)
      }
    }
}





































