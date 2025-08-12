package com.example.bicel.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bicel.LoginActivity
import com.example.bicel.R
import com.example.bicel.SharedPreferences.SharedPreferencesClass
import com.example.bicel.Utility.UtilityClass
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.databinding.FragmentAccountBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Account() : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var userViewModel: UserViewModel
    private lateinit var utilityClass: UtilityClass
    private var profilePicUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Variables
        firebaseAuth = Firebase.auth
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        utilityClass = UtilityClass()
        // Ends here
        val sharedPreferences = SharedPreferencesClass(requireContext())
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            sharedPreferences.setFirstTimeLaunch(true)
            userViewModel.logout()
            activity?.findViewById<BottomNavigationView>(R.id.bottomNavView)?.selectedItemId =
                R.id.home

        }
        userViewModel.isLoggedIn.observe(viewLifecycleOwner) { isloggedIn ->
            activity.let {
            if (isloggedIn || firebaseAuth.currentUser != null) {
                Log.d("Account", "User is logged in")
                // if User is Logged In
                getProfileImage(binding.userImg)
                getUserData()
                binding.progressBar.visibility = View.GONE
                binding.unloggedScreen.visibility = View.GONE
                binding.loggedScreen.visibility = View.VISIBLE

            } else { // if User is not Logged in
                Log.d("Account", "elseeeeeeeeeeeeeeeeeeeeeee")
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(300)
                    binding.unloggedScreen.visibility = View.VISIBLE
                    binding.loggedScreen.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

        binding.editProfileBtn.setOnClickListener {
            resetEditScreen()
            getProfileImage(binding.editUserImg)
            showEditScreen(screenWidth)
            binding.userEmail.text = firebaseAuth.currentUser!!.email.toString()
        }
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
            activity?.finish()
        }

        binding.editProfileScreen.animate()
            .translationX(screenWidth) // Initial position of edit screen

        binding.closeEditProfileBtn.setOnClickListener {
            showAccountScreen(screenWidth)
        } // cross icon listener
        binding.cancelEditBtn.setOnClickListener {
            showAccountScreen(screenWidth)
        } // Cancel button listener

        binding.profilePicSelectBtn.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    1080,
                    1080
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        binding.saveEditBtn.setOnClickListener {
            val profilePicRef = firebaseStorage.getReference("UserImages/${firebaseAuth.currentUser!!.uid}")
            val name = binding.editName.text.toString()
            val phone = binding.editPhone.text.toString()
            val oldName = binding.userName.text.toString()
            val oldPhone = binding.userPhone.text.toString()


            if (oldPhone != phone) { // if phone number is changed
                if (phone.isEmpty()) {
                    binding.editPhone.error = "Please Enter a Phone Number"
                    binding.editPhone.requestFocus()
                }
                else if (!utilityClass.phoneIsValid(phone)) {
                    binding.editPhone.error = "Phone Number is Invalid"
                    binding.editPhone.requestFocus()
                }
                else if (utilityClass.phoneIsValid(phone)){
                    binding.progressBar.visibility = View.VISIBLE
                    binding.editPhone.error = null
                    firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                        .update("phone", phone).addOnSuccessListener {
                            getUserData()
                            showAccountScreen(screenWidth)
                            binding.progressBar.visibility = View.GONE
                        }
                }
                else {binding.editPhone.error = "Something went wrong"}
                }
            if (oldName != name) {// if name is changed
                if (name.isEmpty()) {
                    binding.editName.error = "Please Enter a Name"
                    binding.editName.requestFocus()
                } else if (!utilityClass.nameIsValid(name)) {
                    binding.editName.error = "Name is Invalid"
                    binding.editName.requestFocus()
                } else if (utilityClass.nameIsValid(name)) {
                    binding.editName.error = null
                    binding.progressBar.visibility = View.VISIBLE
                    firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                        .update("name", name).addOnSuccessListener {
                            getUserData()
                            showAccountScreen(screenWidth)
                            binding.progressBar.visibility = View.GONE
                        }
                } else {
                    binding.editName.error = "Something went wrong"
                }
            }





            if (profilePicUri != null && binding.editName.error == null && binding.editPhone.error == null) {
                // if profile pic is selected
                binding.progressBar.visibility = View.VISIBLE
                profilePicRef.putFile(profilePicUri!!).addOnSuccessListener {
                    it.storage.downloadUrl.addOnSuccessListener { uri->
                        firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid)
                            .update("profilePicUri",uri).addOnSuccessListener {
                            getProfileImage(binding.userImg)
                            showAccountScreen(screenWidth)
                            binding.progressBar.visibility = View.GONE
                            }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    binding.progressBar.visibility = View.GONE
                }
            }
            if (profilePicUri == null && oldName == name && oldPhone == phone) {// if no changes
                showAccountScreen(screenWidth)
                binding.progressBar.visibility = View.GONE
            }

        }
    }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            binding.editUserImg.setImageURI(data?.data)
            profilePicUri = data?.data!!

        } else if (resultCode == Activity.RESULT_CANCELED) {

                   getProfileImage(binding.editUserImg)

            }
     else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }


//    fun getProfileImage(imageView: ImageView) {
//                val currentUseruid = firebaseAuth.currentUser!!.uid
//                firebaseStorage.getReference("UserImages/${currentUseruid}").downloadUrl.addOnSuccessListener {
//                    Glide.with(requireContext())
//                        .load(it)
//                        .into(imageView)
//
//                }.addOnFailureListener {
//                    Toast.makeText(
//                        requireContext(),
//                        "Trouble in loading profile picture",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }


    fun getUserData() {
        val currentUseruid = firebaseAuth.currentUser!!.uid
        firebaseFirestore.collection("users").document(currentUseruid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val username = it.getString("name")
                    val userphone = it.getString("phone")
                    binding.userName.text = username
                    binding.userPhone.text = userphone
                    getProfileImage(binding.userImg)
                    // Edit Screen
                    binding.editName.setText(username)
                    binding.editPhone.setText(userphone)
                    getProfileImage(binding.editUserImg)
                }
            }

    }
   fun getProfileImage(imageView: ImageView){
        firebaseFirestore.collection("users").document(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it.exists()){
                    val userProfilePic = it.getString("profilePicUri")
                    Glide.with(requireContext())
                        .load(userProfilePic)
                        .into(imageView)
                }
            }
    }

    fun showAccountScreen(screenWidth: Float) {
        binding.editProfileScreen.animate().apply {
            duration = 300
            translationX(screenWidth)
        }.start()
        binding.loggedScreen.animate().apply {
            duration = 300
            translationX(0f)
        }.start()
    }

    fun showEditScreen(screenWidth: Float) {
        binding.loggedScreen.animate().apply {
            duration = 300
            translationX(-screenWidth)
        }.start()
        binding.editProfileScreen.animate().apply {
            duration = 300
            translationX(0f)
        }.start()
    }
    fun resetEditScreen(){
        binding.editName.clearFocus()
        binding.editPhone.clearFocus()
        binding.editName.error = null
        binding.editPhone.error = null
        profilePicUri = null
    }
}