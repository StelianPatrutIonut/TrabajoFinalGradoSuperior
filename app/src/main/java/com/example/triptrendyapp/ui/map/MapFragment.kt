package com.example.triptrendyapp.ui.map

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.R
import com.example.triptrendyapp.databinding.ActivityMapFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap

    private lateinit var locationEditText: EditText

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var contextWrapper: Context

    private var _binding: ActivityMapFragmentBinding? = null

    private val binding get() = _binding!!

    private var municipiosDistritosBarrios: ArrayList<String> = ArrayList()

    override fun onAttach(context: Context) {

        super.onAttach(context)

        contextWrapper = context

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = ActivityMapFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        locationEditText = binding.locationEditText

        municipiosDistritosBarrios.clear()

        loadMunicipiosFromJson()

        loadBarriosFromJson()

        loadDistritiFromJson()

        val autoCompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, municipiosDistritosBarrios)

        val autoCompleteTextView = locationEditText as AutoCompleteTextView

        autoCompleteTextView.setAdapter(autoCompleteAdapter)

        autoCompleteTextView.threshold = 1

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return root

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    override fun onStop() {

        super.onStop()

        locationEditText.setText("")

        mMap.clear()

    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        mMap.setOnMyLocationButtonClickListener(this)

        mMap.setOnMyLocationClickListener(this)

        enableMyLocation()

        locationEditText.setOnEditorActionListener { _, _, _ ->

            searchLocation()

            true

        }

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

                    val currentLatLng = LatLng(location.latitude, location.longitude)

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


    private fun searchLocation() {

        val locationName = locationEditText.text.toString().trim()

        if (locationName.isNotEmpty()) {

            val geocoder = Geocoder(requireContext())

            try {

                val addresses = geocoder.getFromLocationName(locationName, 1)

                if (addresses != null) {

                    if (addresses.isNotEmpty()) {

                        val address = addresses[0]

                        val latLng = LatLng(address.latitude, address.longitude)

                        mMap.clear()

                        mMap.addMarker(MarkerOptions().position(latLng).title(locationName))

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    } else {

                        Toast.makeText(requireContext(), "Ubicación no encontrada", Toast.LENGTH_SHORT).show()

                    }

                }

            } catch (e: IOException) {

                e.printStackTrace()

            }

        } else {

            Toast.makeText(requireContext(), "Ingrese una ubicación", Toast.LENGTH_SHORT).show()

        }

    }

    private fun loadMunicipiosFromJson() {

        try {

            val inputStream = requireContext().assets.open("municipio_comunidad_madrid.json")

            val size = inputStream.available()

            val buffer = ByteArray(size)

            inputStream.read(buffer)

            inputStream.close()

            val json = JSONObject(String(buffer, Charset.defaultCharset()))

            val municipiosArray = json.getJSONArray("data")

            for (i in 0 until municipiosArray.length()) {

                val municipioObject = municipiosArray.getJSONObject(i)

                val municipioNombre = municipioObject.getString("municipio_nombre")

                municipiosDistritosBarrios.add(municipioNombre)

            }

        } catch (e: IOException) {

            e.printStackTrace()

        }

    }

    private fun loadBarriosFromJson() {

        try {

            val inputStream = requireContext().assets.open("barrios_municipio_madrid.json")

            val size = inputStream.available()

            val buffer = ByteArray(size)

            inputStream.read(buffer)

            inputStream.close()

            val json = JSONObject(String(buffer, Charset.defaultCharset()))

            val municipiosArray = json.getJSONArray("data")

            for (i in 0 until municipiosArray.length()) {

                val municipioObject = municipiosArray.getJSONObject(i)

                val barrioNombre = municipioObject.getString("barrio_nombre").trim() +" " + municipioObject.getString("distrito_nombre").trim()

                municipiosDistritosBarrios.add(barrioNombre)

            }

        } catch (e: IOException) {

            e.printStackTrace()

        }

    }

    private fun loadDistritiFromJson() {

        try {

            val inputStream = requireContext().assets.open("barrios_municipio_madrid.json")

            val size = inputStream.available()

            val buffer = ByteArray(size)

            inputStream.read(buffer)

            inputStream.close()

            val json = JSONObject(String(buffer, Charset.defaultCharset()))

            val municipiosArray = json.getJSONArray("data")

            val municipiosSet = HashSet<String>()

            for (i in 0 until municipiosArray.length()) {

                val municipioObject = municipiosArray.getJSONObject(i)

                val barrioNombre =  municipioObject.getString("distrito_nombre").trim()

                if (!municipiosSet.contains(barrioNombre)) {

                    municipiosSet.add(barrioNombre)

                }
            }

            municipiosDistritosBarrios.addAll(municipiosSet)

        } catch (e: IOException) {

            e.printStackTrace()

        }

    }

}
