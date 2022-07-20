package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.databases.DatabaseHandler
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.model.HappyPlaceModel

class HappyPlaceAdapter(private val context: Context, private val list:ArrayList<HappyPlaceModel>):RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>() {

    private var onClickListener:OnClickListener? = null

    class ViewHolder(binding:ItemHappyPlaceBinding):RecyclerView.ViewHolder(binding.root){
        val photoImageView = binding.ivPlaceImage
        val nameTextView = binding.tvName
        val descriptionTextView = binding.tvDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return  ViewHolder(ItemHappyPlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    fun setOnClickListener(onclickListener:OnClickListener){
        this.onClickListener = onclickListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        //uri,thelink where the image is
        holder.photoImageView.setImageURI(Uri.parse(model.image))
        holder.nameTextView.text = model.name
        holder.descriptionTextView.text = model.description

        //inorder to set the onClickListener
        holder.itemView.setOnClickListener{
            if (onClickListener!=null){
                onClickListener!!.onClick(position,model)
            }
        }
    }

    //inorder to notify the adapter that we would want to delete
    fun removeAt(position: Int){
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteHappyPlace(list[position])
        if (isDeleted > 0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }


    //inorder to notify the adapter that we would want to make changes
    fun notifyEditItem(activity: Activity,position:Int,requestCode:Int){
        //inorder to take us to AddHappyPlacesActivity
        val intent =Intent(context,AddHappyPlaceActivity::class.java)
        //now put extra information to the intent
        //list is the list of typeHappyPlaceModel declared in th constractor
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)

    }



    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position:Int,model:HappyPlaceModel)

    }
}