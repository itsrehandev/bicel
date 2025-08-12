package com.example.bicel.dataClasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

class Ad (
                val title: String = "",
                val description: String = "",
                val price: String = "",
                val category: String = "",
                val uploaderId: String = "",
                val ImagesUrl: ArrayList<String> = arrayListOf(),
                val timeStamp: Timestamp? = null,
){
}