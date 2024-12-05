package com.example.triptrendyapp.ui.myLocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.triptrendyapp.databinding.ActivityMyLocationFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MyLocationFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var contextWrapper: Context

    private var _binding: ActivityMyLocationFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var nombreUsuario : String

    private lateinit var currentLatLng : LatLng

    override fun onAttach(context: Context) {

        super.onAttach(context)

        contextWrapper = context

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = ActivityMyLocationFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        nombreUsuario = arguments?.getString("usuario").toString()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.compartir.setOnClickListener{
            shareRoute()
        }
        return root

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    override fun onStop() {

        super.onStop()

        mMap.clear()

    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        mMap.setOnMyLocationButtonClickListener(this)

        mMap.setOnMyLocationClickListener(this)

        enableMyLocation()


    }

    override fun onMyLocationButtonClick(): Boolean {

        return false

    }

    override fun onMyLocationClick(p0: android.location.Location) {

    }

    private fun enableMyLocation() {

        if (ContextCompat.checkSelfPermission(contextWrapper, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = true

            getLastLocation()

        } else {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }

    }

    private fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(contextWrapper, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextWrapper,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_LOCATION)

            return

        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    currentLatLng = LatLng(location.latitude, location.longitude)

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                }

            }

            .addOnFailureListener { e ->

            }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CODE_LOCATION) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                enableMyLocation()

            }

        }

    }

    companion object {

        private const val REQUEST_CODE_LOCATION = 1

    }

    @SuppressLint("SuspiciousIndentation")
    private fun shareRoute() {

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.type = "text/plain"

        val routeDescription = "¡Echa un vistazo a mi ubicación actual en TripTrendy!\n\n"

        val lugares = StringBuilder()


            lugares.append("Usuario : " + nombreUsuario + "\n")

            lugares.append("Ubicación: Latitud " +  currentLatLng.latitude + " , Longitud " + currentLatLng.longitude + "\n\n")

        val routeDetails = "$routeDescription\n$lugares"

        shareIntent.putExtra(Intent.EXTRA_TEXT, routeDetails)

        startActivity(Intent.createChooser(shareIntent, "Compartir ruta a través de:"))

    }

}
