package com.example.happyplaces.model

import java.io.Serializable

data class HappyPlaceModel(
    val id:Int,
    val name:String,
    val image:String,
    val description:String,
    val date:String,
    val location:String,
    val latitude:Double,
    val longitude:Double

    //inorder to pass this model fro one class into another use serializable
):Serializable
