package com.example.happyplaces.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.media.audiofx.Virtualizer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AlertDialog
import com.example.happyplaces.R
import com.example.happyplaces.databases.DatabaseHandler
import com.example.happyplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplaces.model.HappyPlaceModel
import com.example.happyplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3

    }



    //inorder to use the calendar
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener:DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage:Uri? = null
    private var mLatitude:Double = 0.0
    private var mLongitude:Double = 0.0

    //so as to get the user's location
    private  lateinit var mFusedLocationClient:FusedLocationProviderClient

    //inorder for this activity to know what to edit either afresh or just update an existing one
    private var mHappyPlaceDetails:HappyPlaceModel? = null

    private var binding:ActivityAddHappyPlaceBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)



        //to implement the backButton
        val toolBar = binding?.toolbarAddPlace
        setSupportActionBar(toolBar)
        //to add the backButton
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        //set navigationOnclick on the toolBar
        toolBar?.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient =LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,resources.getString(R.string.google_maps_api_key))

        }

        //so as to create a new entry
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            //assign the model with the details from it
            mHappyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as HappyPlaceModel
        }

        //so as to implement the date
        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()

        }
        updateDateInView()
        //so as to know we are supposed to just edit else we are supposed to create a new entry
        if (mHappyPlaceDetails!= null){
            supportActionBar?.title ="Edit Happy Place"

            binding?.etTitle?.setText(mHappyPlaceDetails!!.name)
            binding?.etDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.etDate?.setText(mHappyPlaceDetails!!.date)
            binding?.etLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)

           binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)

          binding?.btnSave?.text = "UPDATE"
        }

        binding?.etDate?.setOnClickListener(this)
        binding?.tvAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.etLocation?.setOnClickListener(this)
        binding?.tvSelectCurrentLocation?.setOnClickListener(this)
    }

    //so as to get the new location of the user
    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }
//    super.onLocationResult(p0)



    /**
     * A location callback object of fused location provider client where we will get the current location details.
     */
    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            // TODO(Step 2: Call the AsyncTask class fot getting an address from the latitude and longitude.)
            // START
            val addressTask =
                GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    val location = binding?.etLocation
                    location?.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
            // END
        }
    }


    //check if we have the right to check the location of the user
    private fun isLocationEnable():Boolean{
        val locationManger: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View?) {

        when(v!!.id){
            R.id.et_date ->{
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))
                        //inorder to show the datePicker dialog
                    .show()
            }

            R.id.tv_add_image->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo From gallery","Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){
                    _,which ->
                    when(which){
                        0->choosePhotoFromGallery()
                        1->takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            //so as to save details to the database
            R.id.btn_save ->{
                when{
                    binding?.etTitle?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter the Title",Toast.LENGTH_SHORT).show()
                    }

                    binding?.etDescription?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter a description",Toast.LENGTH_SHORT).show()
                    }

                    binding?.etLocation?.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter a location",Toast.LENGTH_SHORT).show()

                    }
                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                    }

                    else ->{
                        //save the dataModel in the database

                        //first create a happyPlaceModel object
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails==null)0 else mHappyPlaceDetails!!.id,
                            binding?.etTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.etDescription?.text.toString(),
                            binding?.etDate?.text.toString(),
                            binding?.etLocation?.text.toString(),
                            mLatitude,
                            mLongitude,
                        )
                        // Here we initialize the database handler class.
                        val dbHandler = DatabaseHandler(this)

                        if (mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                            if (addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                        else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                            if (updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }

                        }




                    }

                }


            }
//            R.id.et_location ->{
//                try {
//                    // These are the list of fields which we required is passed
//                    val fields = listOf(
//                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
//                        Place.Field.ADDRESS
//                    )
//                    // Start the autocomplete intent with a unique request code.
//                    val intent =
//                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
//                            .build(this@AddHappyPlaceActivity)
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
            R.id.tv_select_current_location ->{
                if (!isLocationEnable()){
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                else {
                    // For Getting current location of user please have a look at below link for better understanding
                    // https://www.androdocs.com/kotlin/getting-current-location-latitude-longitude-in-android-using-kotlin.html
                    Dexter.withActivity(this)
                        .withPermissions(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {

                                  requestNewLocationData()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }
            }

                }





    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK){
            if (requestCode== GALLERY){
                if (data!=null){
                    val contentUri = data.data
                    try {
                        //try and get the media from the media store and the contentUri
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                         saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved Image","Path::$saveImageToInternalStorage")
                        val ivImage = binding?.ivPlaceImage
                        ivImage?.setImageBitmap(selectedImageBitmap)
                    }
                    catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,"Failed tp load the image from gallery",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (requestCode== CAMERA){
                val thumbnail:Bitmap = data!!.extras!!.get("data") as Bitmap
                 saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                Log.e("Saved Image","Path::$saveImageToInternalStorage")

                val ivImage = binding?.ivPlaceImage
                ivImage?.setImageBitmap(thumbnail)
            }
//            else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
//                val place:Place = Autocomplete.getPlaceFromIntent(data!!)
//                val location = binding?.etLocation
//                location?.setText(place.address)
//                mLatitude = place.latLng!!.latitude
//                mLongitude = place.latLng!!.longitude
//
//            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            @Override
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {if (report!!.areAllPermissionsGranted()){
                //create a gallery intent that takes you to the media storage
                val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE
                    )
                startActivityForResult(galleryIntent,CAMERA)



            }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()

    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            @Override
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {if (report!!.areAllPermissionsGranted()){
                //create a gallery intent that takes you to the media storage
                val galleryIntent = Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,GALLERY)



            }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {
                showRationalDialogForPermissions()
            }

        }).onSameThread().check()
    }

    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("" +"It looks like you have turned off permissions required for this feature. It can be enable under the Applications Settings")
            .setPositiveButton("GO TO THE SETTINGS"){
                _,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    var uri = Uri.fromParts("package",packageName,null)
                    intent.data=uri
                    startActivity(intent)
                }
                catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){dialog,_ ->
                dialog.dismiss()

            }
            .show()

    }

    private fun updateDateInView(){
        //when working with dates you need to have a certain format
        val myFormat = "dd.MM.yyyy"
        val sdf =SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(mBitmap:Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream:OutputStream=FileOutputStream(file)
            mBitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()

        }
        catch (e:IOException){

        }

        return Uri.parse(file.absolutePath)

    }

}