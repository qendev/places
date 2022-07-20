package com.example.happyplaces.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.adapters.HappyPlaceAdapter
import com.example.happyplaces.databases.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.model.HappyPlaceModel
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback


class MainActivity : AppCompatActivity() {

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE =1
        var EXTRA_PLACE_DETAILS= "extra_place_details"

    }

    private var binding:ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        //move to AddHappyPlaceActivity using an intent
        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this,AddHappyPlaceActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlaceListFromLocalDB()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // check if the request code is same as what is passed  here it is 'ADD_PLACE_ACTIVITY_REQUEST_CODE'
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getHappyPlaceListFromLocalDB()
            }else{
                Log.e("Activity", "Cancelled or Back Pressed")
            }
        }
    }


    private fun getHappyPlaceListFromLocalDB(){
        //create an object of the dbHandler inorder to call this function
        val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList:ArrayList<HappyPlaceModel> =dbHandler.getHappyPlacesList()
        //check if the list is not empty then go through the list
        if (getHappyPlaceList.size > 0){
            //first make the recyclerView visible
            val rvHappyPlaces = binding?.recyclerViewHappyPlacesList
            rvHappyPlaces?.visibility = View.VISIBLE
            //hide the no record textview
            val noRecordtextView = binding?.textViewNoRecords
            noRecordtextView?.visibility = View.GONE
            //now inorder populate the recyclerView
            populateHappyPlacesRecyclerView(getHappyPlaceList)
        }
        else{
            val rvHappyPlaces = binding?.recyclerViewHappyPlacesList
            rvHappyPlaces?.visibility = View.GONE
            val noRecordTextView = binding?.textViewNoRecords
            noRecordTextView?.visibility = View.VISIBLE

        }



    }


    private fun populateHappyPlacesRecyclerView(happyPlaceList:ArrayList<HappyPlaceModel>){
        val rvHappyPlaces = binding?.recyclerViewHappyPlacesList
        //set the recyclerView to the layoutManager
        rvHappyPlaces?.layoutManager = LinearLayoutManager(this)
        //set the recyclerView to the adapter
        //first define the adapter
        val placesAdapter = HappyPlaceAdapter(this, happyPlaceList)
        //now set the recyclerView to the adapter
        rvHappyPlaces?.adapter = placesAdapter
        placesAdapter.setOnClickListener(object :HappyPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,HappyPlaceDetailActivity::class.java)
                //put extra into the intent before startin the activity
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })
        //bind the swipe to edit feature to the recyclerView
        val editSwapHandler = object:SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //inorder to call the fun notifyEditItem
                //create a new adapter
                val adapter = rvHappyPlaces?.adapter as HappyPlaceAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)

            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwapHandler)
        rvHappyPlaces?.setHasFixedSize(true)
        //attach to the recyclerView
        editItemTouchHelper.attachToRecyclerView(rvHappyPlaces)

        //bind the swipe to delete feature to the recyclerView
        val deleteSwapHandler = object: SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //inorder to call the fun notifyEditItem
                //create a new adapter
                val adapter = rvHappyPlaces?.adapter as HappyPlaceAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlaceListFromLocalDB()

            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwapHandler)
        rvHappyPlaces?.setHasFixedSize(true)
        //attach to the recyclerView
        editItemTouchHelper.attachToRecyclerView(rvHappyPlaces)
        deleteItemTouchHelper.attachToRecyclerView(rvHappyPlaces)




    }

}