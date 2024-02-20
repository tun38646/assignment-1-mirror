package edu.temple.convoy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class ConvoyViewModel : ViewModel() {
    private val location by lazy {
        MutableLiveData<LatLng>()
    }

    private val convoyId by lazy {
        MutableLiveData<String>()
    }

    fun setConvoyId(id: String) {
        convoyId.value = id
    }

    fun setLocation(latLng: LatLng) {
        location.value = latLng
    }

    fun getLocation(): LiveData<LatLng> {
        return location
    }

    fun getConvoyId(): LiveData<String> {
        return convoyId
    }
}