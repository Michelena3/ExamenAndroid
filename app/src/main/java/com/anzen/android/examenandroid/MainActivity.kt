package com.anzen.android.examenandroid

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.anzen.android.examenandroid.databinding.ActivityMainBinding
import com.anzen.android.examenandroid.helpers.JsonReaderHelper
import com.anzen.android.examenandroid.utils.EcobiciContentModel
import com.anzen.android.examenandroid.utils.ResponseListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.orhanobut.hawk.Hawk
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error

class MainActivity : AppCompatActivity(), AnkoLogger, ResponseListener<List<EcobiciContentModel>>,
    OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private lateinit var jsonReaderHelper: JsonReaderHelper
    var arrayObject = arrayListOf<EcobiciContentModel>()
    var arrayNames = arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        Hawk.init(this).build()
        jsonReaderHelper = JsonReaderHelper(this)
        jsonReaderHelper.getInfoBikes(this)
        binding.progressView.visibility = View.GONE

        binding.btnOpenAlert.setOnClickListener {
            val mDialogView =
                LayoutInflater.from(this).inflate(R.layout.alert_main, null)
            val mBuilder = AlertDialog.Builder(this, R.style.MyDialogThemeBG)
                .setView(mDialogView).create()
            mBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val itemList = mDialogView.findViewById<ListView>(R.id.item_list)
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayNames.reversed())
            itemList.adapter = adapter
            itemList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                packageManager?.let {
                    val url = "waze://?ll=${Hawk.get("closestLatitude", 0.0)},${Hawk.get("closestLongitude", 0.0)}&navigate=yes"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.resolveActivity(it)?.let {
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(
                            this,
                            "Waze no fue encontrado o no está instalado...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            val btnOpcion1 = mDialogView.findViewById<Button>(R.id.btnOpcion1)
            btnOpcion1.setOnClickListener {
                mBuilder.dismiss()
            }
            mBuilder.show()
        }
    }

    override fun onSuccess(responseObject: List<EcobiciContentModel>) {
        for (item in responseObject) {
            arrayObject.add(item)
        }
    }

    override fun onError(error: String) {
        error { error }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        binding.ciudad.text = "Ciudad de México"
        for (i in arrayObject) {
            if (i.bikes == "0") {
                error { "Esta es una de las estaciones que no tiene bicis... ${i.name}" }
            } else {
                googleMap!!.addMarker(
                    MarkerOptions()
                        .position(LatLng(i.lat!!.toDouble(), i.lon!!.toDouble()))
                        .title(i.name)
                        .snippet("Número de bicis disponibles: ${i.bikes}")
                ).showInfoWindow()
                googleMap.setOnInfoWindowClickListener {
                    val latitude = it.position.latitude
                    val longitude = it.position.longitude
                    packageManager?.let {
                        val url = "waze://?ll=$latitude,$longitude&navigate=yes"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.resolveActivity(it)?.let {
                            startActivity(intent)
                        } ?: run {
                            Toast.makeText(
                                this,
                                "Waze no fue encontrado o no está instalado...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
        @SuppressLint("MissingPermission")
        googleMap!!.isMyLocationEnabled = true
        googleMap.setMinZoomPreference(14.0f)
        googleMap.setMaxZoomPreference(2000.0f)
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            val currentLocation = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(currentLocation).title("Ubicación Actual"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
            error { currentLocation }
            var closestLocation: Location?
            var smallestDistance = -1
            for (location in arrayObject) {
                val arrayName = arrayListOf<String>()
                val startPoint = Location("Ubicación")
                startPoint.latitude = it.latitude
                startPoint.longitude = it.longitude
                val endPoint = Location("Destinos")
                endPoint.latitude = location.lat!!.toDouble()
                endPoint.longitude = location.lon!!.toDouble()
                val distance: Float = startPoint.distanceTo(endPoint)
                if (smallestDistance == -1 || distance < smallestDistance) {
                    closestLocation = endPoint
                    smallestDistance = distance.toInt()
                    arrayName.add(location.name!!)
                    arrayNames.add(arrayName.toString().removeRange(0, 1).replace("]".toRegex(), ""))
                    arrayNames
                    error { "List of Names: ${arrayName.size}" }
                    error { "Endpoint: (${closestLocation.latitude},${closestLocation.longitude}) y el nombre es ${location.name}" }
                    Hawk.put("closestLatitude", closestLocation.latitude)
                    Hawk.put("closestLongitude", closestLocation.longitude)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        googleMap.isMyLocationEnabled = true
    }
}
