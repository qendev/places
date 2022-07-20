package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMapBinding
import com.example.happyplaces.model.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(),OnMapReadyCallback {
    private var binding:ActivityMapBinding? = null

    //create an object of the model class
    private var mHappyPlaceDetails:HappyPlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        //retrieve data from intent
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        if (mHappyPlaceDetails != null){
            val toolBarMap = binding?.toolbarMap
            setSupportActionBar(toolBarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetails!!.name

            toolBarMap?.setNavigationOnClickListener {
                onBackPressed()

            }
            //create support mapFragment
            val supportMapFragment:SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)


        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(mHappyPlaceDetails!!.latitude,mHappyPlaceDetails!!.longitude)

        //assign a marker on the google map
        googleMap.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetails!!.location))
        //so as to zoom in on the map
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,10f)
        //so as to animate the google map
        googleMap.animateCamera(newLatLngZoom)
    }
}