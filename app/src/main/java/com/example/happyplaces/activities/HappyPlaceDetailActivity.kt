package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.model.HappyPlaceModel

class HappyPlaceDetailActivity : AppCompatActivity() {
    private var binding:ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //inorder to retrieve the data from the model
        //first craete an object of the model
        var happyPlaceDetailModel:HappyPlaceModel? = null
        //check if the intent has any extra data
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetailModel =intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (happyPlaceDetailModel!=null){
            val toolBar = binding?.toolbarHappyPlaceDetail
            setSupportActionBar(toolBar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = happyPlaceDetailModel.name

            toolBar?.setNavigationOnClickListener {
                onBackPressed()
            }
            //first initialize all the necessary views
            val imageView = binding?.ivPlaceImage
            val textDescription = binding?.tvDescription
            val textLocation = binding?.tvLocation

            imageView?.setImageURI(Uri.parse(happyPlaceDetailModel.image))
            textDescription?.text = happyPlaceDetailModel.description
            textLocation?.text = happyPlaceDetailModel.location


            binding?.btnViewOnMap?.setOnClickListener{
                val intent = Intent(this,MapActivity::class.java)
                //put extra data inside the intent
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,happyPlaceDetailModel)
                startActivity(intent)

            }

        }


    }
}