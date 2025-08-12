package com.example.bicel.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.bicel.R
import com.example.bicel.ViewModels.UserViewModel
import com.example.bicel.adapters.adViewPager
import com.example.bicel.databinding.AdImagePicker4Binding
import com.example.bicel.databinding.DiscardAdDialogBinding
import com.example.bicel.databinding.FragmentSellBinding
import com.example.bicel.databinding.UploadSuccessDialog4Binding
import com.example.bicel.databinding.UploadingDialog4Binding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


class Sell : Fragment() {
    private lateinit var binding: FragmentSellBinding
    private var adImagesUri = ArrayList<String>()
    private lateinit var adViewPagerAdapter: adViewPager
    private lateinit var userViewModel: UserViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private var adImagesDownloadUrl = ArrayList<Uri>()
    private var uploadingDialog: AlertDialog? = null
    private lateinit var myAdData: Bundle
    var isUpdating: Boolean = false
    override fun onResume() {
        super.onResume()
        val categories = resources.getStringArray(R.array.categories)
        val categoriesArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.category_spinner_4, categories)
        binding.categoryAutoComplete.setAdapter(categoriesArrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSellBinding.inflate(inflater, container, false)

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        Variables
        adViewPagerAdapter = adViewPager(this)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        firebaseAuth = Firebase.auth
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        myAdData = Bundle()
        //Ends
        userViewModel.isLoggedIn.observe(viewLifecycleOwner){isLoggedIn->
            if(isLoggedIn || firebaseAuth.currentUser != null){
                binding.noLoginScreen.visibility = View.GONE
                binding.sellScreen.visibility = View.VISIBLE
            }else{
                binding.noLoginScreen.visibility = View.VISIBLE
                binding.sellScreen.visibility = View.GONE

            }
        }
        val adViewPager = binding.adViewPager
        adViewPager.adapter = adViewPagerAdapter
        binding.addImageBtn.setOnClickListener {
            showAdImagePickDialog()
        }
        binding.addImageIcon.setOnClickListener {
            showAdImagePickDialog()
        }

        binding.categoryAutoComplete.setOnItemClickListener { parent, view, position, id ->
            isAdDataPresent()
        }
        binding.price.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    if (binding.price.text.toString().toInt() !in 100..99999999 && s.isNotEmpty()) {
                        binding.price.error = "Price must be between 100 and 99999999"
                    } else {
                        binding.price.error = null
                    }
                }
                isAdDataPresent()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.title.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length !in 20..80 && !s.isNullOrEmpty()) {
                    binding.title.error = "Title must be between 20 and 80 characters"
                } else {
                    binding.title.error = null
                }
                isAdDataPresent()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.description.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                binding.descCount.text = "$currentLength/700"
                isAdDataPresent()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.uploadBtn.setOnClickListener {
            if (validateAdData()) {
                showDiscardDialog(false)
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_LONG)
                    .show()
            }


        }
        binding.cancelBtn.setOnClickListener {
            if (isAdDataPresent()) showDiscardDialog(true)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        binding.adImageProgress.visibility = View.GONE
        if (resultCode == Activity.RESULT_OK) {
            val adImageUri = data?.data!!.toString()
            adImagesUri.add(adImageUri)
            adViewPagerAdapter.setList(adImagesUri)
            binding.adImagesCount.text = "${adImagesUri.size}/5"
            binding.adViewPager.setCurrentItem(adImagesUri.size - 1, false)
            binding.delImageBtn.visibility = View.VISIBLE
            if (adImagesUri.size == 5) {
                binding.addImageBtn.alpha = 0.4f
            }
            isAdDataPresent()
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
        }
        if (adImagesUri.isEmpty()) {
            binding.addImageIcon.visibility = View.VISIBLE
            binding.delImageBtn.visibility = View.GONE
        }
//        lifecycleScope.launch(Dispatchers.Main) {
//            delay(240)
//            if (binding.adImageProgress.visibility == View.VISIBLE) {
//                binding.adImageProgress.visibility = View.GONE
//            }
//
//        }
    }

    fun pickAdImage(isGallery: Boolean) {
        if (adImagesUri.size < 5 && isGallery) {
            ImagePicker.with(this)
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .galleryOnly()
                .start()
        } else if (adImagesUri.size < 5) {
            ImagePicker.with(this)
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .cameraOnly()
                .start()
        }
        lifecycleScope.launch(Dispatchers.Main) {
            delay(570)
            binding.adImageProgress.visibility = View.VISIBLE
            binding.addImageIcon.visibility = View.GONE
        }
    }

    fun isAdDataPresent(): Boolean {
        isAdUpdatable()
        if (binding.price.text.toString().isNotEmpty()
            || binding.title.text.toString().isNotEmpty()
            || binding.description.text.toString().isNotEmpty()
            || binding.categoryAutoComplete.text.toString().isNotEmpty()
            || adImagesUri.isNotEmpty()
        ) { // If any field has anything means there is some data and show discard ad warning dialog
            userViewModel.uploadingAd()
            return true
        } else {
            userViewModel.cancellingAd()
            return false
        }

    }

    fun discardAdData() {
        binding.price.text.clear()
        binding.title.text.clear()
        binding.description.text.clear()
        binding.price.clearFocus()
        binding.title.clearFocus()
        binding.description.clearFocus()
        binding.price.error = null
        binding.title.error = null
        binding.description.error = null

        binding.categoryAutoComplete.setText("")
        binding.categoryAutoComplete.clearFocus()
        adImagesDownloadUrl.clear()
        adImagesUri.clear()
        adViewPagerAdapter.setList(adImagesUri)
        binding.addImageIcon.visibility = View.VISIBLE
        binding.delImageBtn.visibility = View.GONE
        binding.addImageBtn.alpha = 1f
        binding.adImagesCount.text = "${adImagesUri.size}/5"
        binding.uploadBtn.text = "Upload Ad"
        userViewModel.cancellingAd()
    }

    fun showAdImagePickDialog() {
        if (adImagesUri.size < 5) {
            val adImagePickerBinding = AdImagePicker4Binding.inflate(layoutInflater)
            val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.BottomAlertDialog)
                .setView(adImagePickerBinding.root)
            val dialog = dialogBuilder.create()
            dialog.apply {
                window?.setGravity(Gravity.BOTTOM)
                setCancelable(true)
                show()

            }

            adImagePickerBinding.cameraBtn.setOnClickListener {
                pickAdImage(false)
                dialog.dismiss()
            }
            adImagePickerBinding.galleryBtn.setOnClickListener {
                pickAdImage(true)
                dialog.dismiss()
            }
            adImagePickerBinding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    fun uploadAd() {
        showUploadingDialog()
        if (!isUpdating) {
            adImagesUri.forEach { currentUri ->
                val adImagesRef =
                    firebaseStorage.reference.child("UsersAdImages/${firebaseAuth.currentUser?.uid}/${UUID.randomUUID()}")
                adImagesRef.putFile(currentUri.toUri())
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { uri ->
                            adImagesDownloadUrl.add(uri)
                            if (adImagesUri.size == adImagesDownloadUrl.size) {
                                Log.d("adImagesDownloadUrl", adImagesDownloadUrl.toString())
                                val adData = hashMapOf(
                                    "title" to binding.title.text.toString(),
                                    "description" to binding.description.text.toString(),
                                    "price" to binding.price.text.toString(),
                                    "category" to binding.categoryAutoComplete.text.toString(),
                                    "uploaderId" to firebaseAuth.currentUser?.uid,
                                    "ImagesUrl" to adImagesDownloadUrl,
                                    "timeStamp" to FieldValue.serverTimestamp()
                                )

                                firebaseFirestore.collection("userAds")
                                    .document(UUID.randomUUID().toString())
                                    .set(adData).addOnSuccessListener {
                                        discardAdData()
                                        hideUploadingDialog()
                                        showUploadSuccessDialog()
                                        userViewModel.adUploaded()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            requireContext(),
                                            it.localizedMessage,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }

                        }

                    }.addOnFailureListener {
                        Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_LONG)
                            .show()

                    }

            }
        } else { // Update AD
            val deletedImages = (myAdData.getStringArrayList("adImages") as ArrayList).filter {
                it !in adImagesUri
            }
            deletedImages.forEach{
                firebaseStorage.getReferenceFromUrl(it).delete().addOnSuccessListener {
                    Log.d("updatingerrors", "1.98 success")
                }.addOnFailureListener {
                    Log.d("updatingerrors", "3"+it.localizedMessage)
                }
            }
            // Previous Images that are still present
            val prevImages = (myAdData.getStringArrayList("adImages") as ArrayList).filter {
                it in adImagesUri
            } as ArrayList<String>
            Log.d("updatingerrors", "6"+prevImages.toString())
            prevImages.forEach {
                adImagesDownloadUrl.add(it.toUri())
            }
            val newImages = adImagesUri.filter { // Images that are newly added
                it !in (myAdData.getStringArrayList("adImages") as ArrayList)
            } as ArrayList<String>
            Log.d("updatingerrors", "2"+newImages.toString())
            val uploadTask = newImages.map { imguri ->
                val adImagesRef =
                    firebaseStorage.reference.child("UsersAdImages/${firebaseAuth.currentUser?.uid}/${UUID.randomUUID()}")
                adImagesRef.putFile(imguri.toUri()).continueWithTask {
                    if (!it.isSuccessful) throw it.exception!!
                    adImagesRef.downloadUrl

                }
            }
            Tasks.whenAllSuccess<Uri>(uploadTask).addOnSuccessListener {
                it.forEach{
                    adImagesDownloadUrl.add(it)
                }
            }.addOnCompleteListener {
                Log.d(
                    "updatingerrors",
                    "sizes ${adImagesDownloadUrl.size}      +      ${adImagesUri.size}"
                )
                if (adImagesUri.size == adImagesDownloadUrl.size) {
                    val newAdData = hashMapOf(
                        "title" to binding.title.text.toString(),
                        "description" to binding.description.text.toString(),
                        "price" to binding.price.text.toString(),
                        "category" to binding.categoryAutoComplete.text.toString(),
                        "uploaderId" to firebaseAuth.currentUser?.uid,
                        "ImagesUrl" to adImagesDownloadUrl,
                        "timeStamp" to myAdData.get("adTime")
                    )
                    firebaseFirestore.collection("userAds")
                        .whereEqualTo("timeStamp", myAdData.get("adTime"))
                        .whereEqualTo("uploaderId", firebaseAuth.currentUser?.uid).get()
                        .addOnSuccessListener {
                            it.documents[0].reference.update(newAdData).addOnSuccessListener {
                                discardAdData()
                                hideUploadingDialog()
                                showUploadSuccessDialog()
                                userViewModel.adUploaded()

                            }
                                .addOnFailureListener {
                                    hideUploadingDialog()
                                    Toast.makeText(
                                        requireContext(),
                                        it.localizedMessage,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                }
            }
        }
    }

    fun showDiscardDialog(isDiscard: Boolean) {
        val dialogBoxBinding: DiscardAdDialogBinding =
            DiscardAdDialogBinding.inflate(layoutInflater)
        val dialogBuilder =
            androidx.appcompat.app.AlertDialog.Builder(
                requireContext(),
                R.style.CustomAlertDialog
            )
                .setView(dialogBoxBinding.root)
        val discardDialogBox = dialogBuilder.create()
        discardDialogBox.apply {
            setCancelable(false)
            show()
        }
        if (isDiscard) {
            dialogBoxBinding.discardBtn.text = "Discard"
            dialogBoxBinding.discardBtn.setBackgroundResource(R.drawable.dialog_discard_btn_bg)
            dialogBoxBinding.message.setText(R.string.discard_ad_message)
            dialogBoxBinding.discardBtn.setOnClickListener {
                discardAdData()
                discardDialogBox.dismiss()
            }

        } else {
            if (isUpdating) {
                dialogBoxBinding.discardBtn.text = "Update"
                dialogBoxBinding.message.text = "Are you sure to Update this Ad?"
            } else {
                dialogBoxBinding.discardBtn.text = "Upload"
                dialogBoxBinding.message.setText(R.string.upload_ad_message)
            }
            dialogBoxBinding.discardBtn.setBackgroundResource(R.color.colorPrimary)
            dialogBoxBinding.discardBtn.setOnClickListener {
                uploadAd()
                discardDialogBox.dismiss()
            }
        }
        dialogBoxBinding.cancelBtn.setOnClickListener {
            discardDialogBox.dismiss()
        }

    }

    fun validateAdData(): Boolean {
        if (binding.title.error == null
            && binding.description.error == null
            && binding.price.error == null
            && binding.categoryAutoComplete.text.toString().isNotEmpty()
            && adImagesUri.isNotEmpty()
        ) {
            return true
        } else {
            return false
        }
    }

    fun showUploadingDialog() {
        if (uploadingDialog == null) {
            val uploadDialogBinding = UploadingDialog4Binding.inflate(layoutInflater)
            val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                .setView(uploadDialogBinding.root)
            uploadingDialog = dialogBuilder.create()
            uploadingDialog?.apply {
                setCancelable(false)
            }
            if (isUpdating) {
                uploadDialogBinding.tv.text = "Updating"
            }else{
                uploadDialogBinding.tv.text = "Uploading"
            }
        }
        uploadingDialog?.show()
    }

    fun hideUploadingDialog() {
        uploadingDialog?.dismiss()
    }

    fun showUploadSuccessDialog() {
        val uploadSuccessDialogBinding = UploadSuccessDialog4Binding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(uploadSuccessDialogBinding.root)
        val dialog = builder.create()
        if (isUpdating) {
            uploadSuccessDialogBinding.tv1.text = "Successfully updated"
        } else{
            uploadSuccessDialogBinding.tv1.text = "Successfully uploaded"
        }
        dialog.apply {
            setCancelable(true)
            show()
        }
        uploadSuccessDialogBinding.okBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    fun deleteAdImg(position: Int) {
        binding.delImageBtn.setOnClickListener {
            val deleteAdBinding: DiscardAdDialogBinding =
                DiscardAdDialogBinding.inflate(LayoutInflater.from(requireContext()))
            val dialog = androidx.appcompat.app.AlertDialog.Builder(
                requireContext(),
                R.style.CustomAlertDialog
            )
                .setView(deleteAdBinding.root).create()
            deleteAdBinding.message.text = "Do you really want to delete this Image?"
            deleteAdBinding.discardBtn.text = "Delete"
            dialog.show()
            deleteAdBinding.cancelBtn.setOnClickListener {
                dialog.dismiss()
            }
            deleteAdBinding.discardBtn.setOnClickListener {
                adImagesUri.removeAt(position)
                adViewPagerAdapter.setList(adImagesUri)
                binding.adImagesCount.text = "${adImagesUri.size}/5"
                if (adImagesUri.size == 0) {
                    binding.delImageBtn.visibility = View.GONE
                    binding.addImageIcon.visibility = View.VISIBLE
                }
                if (binding.addImageBtn.alpha != 1f) {
                    binding.addImageBtn.alpha = 1f
                }
                dialog.dismiss()
                isAdDataPresent()
            }
        }
    }


    fun insertAdData(prevAdData: Bundle) {
        //  Fetching & Inserting Data
        val imagesList = prevAdData.getStringArrayList("adImages") as ArrayList
        val category = prevAdData.getString("adCategory")
        val title = prevAdData.getString("adTitle")
        val description = prevAdData.getString("adDescription")
        val price = prevAdData.getString("adPrice")

        adViewPagerAdapter.setList(imagesList)
        binding.categoryAutoComplete.setText(category)
        binding.title.setText(title)
        binding.description.setText(description)
        binding.price.setText(price)
        // Arrangements in UI
        binding.addImageIcon.visibility = View.GONE
        binding.delImageBtn.visibility = View.VISIBLE
        binding.adImagesCount.text = "${imagesList.size}/5"
        if (imagesList.size == 5) {
            binding.addImageBtn.alpha = 0.4f
        }
        binding.uploadBtn.text = "Update Ad"
        binding.uploadBtn.alpha = 0.4f
        binding.uploadBtn.isClickable = false
        isUpdating = true
        adImagesUri.addAll(imagesList)
        myAdData.putAll(prevAdData)
        isAdDataPresent()
    }

    fun isAdUpdatable() {
        if (!myAdData.isEmpty) {
            val previousImages = myAdData.getStringArrayList("adImages")
            if (adImagesUri == previousImages
                && binding.title.text.toString() == myAdData.getString("adTitle")
                && binding.description.text.toString() == myAdData.getString("adDescription")
                && binding.price.text.toString() == myAdData.getString("adPrice")
            ) { // Nothing is changed
                binding.uploadBtn.alpha = 0.4f
                binding.uploadBtn.isClickable = false
            } else {
                binding.uploadBtn.isClickable = true
                binding.uploadBtn.alpha = 1f
            }
        }
    }

    fun extractPathFromUrl(imageUrl: String): String {
        return try {
            val decodedUrl = Uri.parse(imageUrl).path ?: return ""
            val index = decodedUrl.indexOf("/o/")
            val pathSegment = if (index != -1) decodedUrl.substring(index + 3) else ""
            Uri.decode(pathSegment.split("?alt=")[0]) // clean path
        } catch (e: Exception) {
            ""
        }
    }
}




