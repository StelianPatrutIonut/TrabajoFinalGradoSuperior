package com.example.triptrendyapp.ui.route

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.R
import com.example.triptrendyapp.databinding.ActivityRouteFragmentBinding
import com.example.triptrendyapp.databinding.ItemRouteCardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class RouteFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private var _binding: ActivityRouteFragmentBinding? = null

    private var todosLosLugares: MutableList<Map<String, String>> = mutableListOf()

    private var lugaresSeleccionados: MutableList<Map<String, String>> = mutableListOf()

    private lateinit var locationEditText: AutoCompleteTextView

    private val binding get() = _binding!!

    private var placeNames: MutableList<String> = mutableListOf()

    private lateinit var mMap: GoogleMap

    private lateinit var nombreUsuario : String

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var contextWrapper: Context

    private lateinit var currentLatLng : LatLng

    private val storage = Firebase.storage

    private val storageRef = storage.reference.child("img_chat")

    private var savedLugaresSeleccionados:  MutableList<Map<String, String>> = mutableListOf()

    private var savedNombreUsuario: String = ""

    private var nombreLugar : ArrayList<String> = ArrayList()

    override fun onAttach(context: Context) {

        super.onAttach(context)

        contextWrapper = context

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = ActivityRouteFragmentBinding.inflate(inflater, container, false)

        val view = binding.root

        binding.layoutPublicacion.visibility = View.GONE

        nombreUsuario = arguments?.getString("usuario").toString()

        leerDatos { listaNombres ->

            nombreLugar = listaNombres

        }

        fetchRandomData()

        locationEditText = binding.locationEditText

        val autoCompleteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, placeNames)

        locationEditText.setAdapter(autoCompleteAdapter)

        locationEditText.threshold = 1

        locationEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                displayRandomData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {} })

        binding.btnGenerarRuta.setOnClickListener {

            showSelectedLocations()

        }

        binding.compartir.setOnClickListener{

            if (lugaresSeleccionados.isNotEmpty()) {

                shareRoute(lugaresSeleccionados)

            } else {

                Toast.makeText(requireContext(), "No se ha generado ninguna ruta aleatoria.", Toast.LENGTH_LONG).show()

            }

        }

        binding.favoritos.setOnClickListener {

            if (lugaresSeleccionados.isNotEmpty()) {

                insertarDatos(nombreUsuario, lugaresSeleccionados)

            } else {

                Toast.makeText(requireContext(),"No se ha generado ninguna ruta aleatoria.", Toast.LENGTH_LONG).show()

            }

        }

        binding.publicacion.setOnClickListener{

            savedNombreUsuario = nombreUsuario
            savedLugaresSeleccionados = lugaresSeleccionados

            if (lugaresSeleccionados.isNotEmpty()) {

                val like: Int = 0

                binding.layoutPublicacion.visibility = View.VISIBLE
                binding.compartir.visibility = View.GONE
                binding.favoritos.visibility = View.GONE
                binding.publicacion.visibility = View.GONE
                binding.scrollRoute.visibility = View.GONE
                binding.fragmentContainer.visibility = View.GONE


                binding.btnCamara.setOnClickListener {

                    startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

                }

                binding.btnGaleria.setOnClickListener {

                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                    galleryLauncher.launch(intent)

                }

                binding.btnActualizar.setOnClickListener {

                    val comentario = binding.eTextComentario.text.toString()

                    if (comentario.isEmpty()) {

                        Toast.makeText(requireContext(), "No se ha puesto ningún comentario.", Toast.LENGTH_LONG).show()

                        return@setOnClickListener

                    }

                    if (binding.iViewFotoSeleccionnada.drawable == null) {

                        Toast.makeText(requireContext(), "No se ha puesto ninguna imagen.", Toast.LENGTH_LONG).show()

                        return@setOnClickListener

                    }


                    val imageBitmap = (binding.iViewFotoSeleccionnada.drawable as BitmapDrawable).bitmap

                    val timestamp = System.currentTimeMillis().toString()

                    val nombreImagen = "$timestamp.jpg"

                    uploadImageToFirebaseStorage(imageBitmap, nombreImagen, comentario, savedNombreUsuario, like)

                    binding.eTextComentario.setText("")
                    binding.iViewFotoSeleccionnada.setImageBitmap(null)

                    binding.layoutPublicacion.visibility = View.GONE
                    binding.compartir.visibility = View.VISIBLE
                    binding.favoritos.visibility = View.VISIBLE
                    binding.publicacion.visibility = View.VISIBLE
                    binding.scrollRoute.visibility = View.VISIBLE
                    binding.fragmentContainer.visibility = View.VISIBLE

                    nombreUsuario = savedNombreUsuario
                    lugaresSeleccionados = savedLugaresSeleccionados

                    findAndDrawOptimizedRoute()

                }

            } else {

                Toast.makeText(requireContext(), "No se ha generado ninguna ruta aleatoria.", Toast.LENGTH_LONG).show()

            }

        }

        return view
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK && result.data != null) {

            val imageUri = result.data?.data

            if (imageUri != null) {

                val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

                binding.iViewFotoSeleccionnada.setImageBitmap(bitmap)

            } else {

                Toast.makeText(requireContext(), "Error al seleccionar la imagen", Toast.LENGTH_SHORT).show()

            }

        }

    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, nombreImagen: String, comentario: String, nombreUsuario: String, like: Int) {

        val imageRef = storageRef.child(nombreImagen)

        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Log.d("MainActivity", "Imagen subida exitosamente")

                crearNuevaPublicacion(nombreImagen, comentario, nombreUsuario, like, savedLugaresSeleccionados)

            } else {

                Log.e("MainActivity", "Error al subir la imagen: ${task.exception}")

            }

        }

    }

    private fun crearNuevaPublicacion(nombreImagen: String, comentario: String, nombreUsuario: String, like: Int, lugares: List<Map<String, String>>) {

        val collectionRef = Firebase.firestore.collection("chat")

        val datosDocumento = hashMapOf("nombreImagen" to nombreImagen,"comentario" to comentario,
            "lugares" to lugares.map { lugar ->
                hashMapOf<String, Any>("nombre" to (lugar["NOMBRE"] ?: ""),
                    "latitud" to (lugar["LATITUD"] ?: ""),
                    "longitud" to (lugar["LONGITUD"] ?: "")) }, "nombreUsuario" to nombreUsuario)

        collectionRef.add(datosDocumento).addOnSuccessListener {

            Toast.makeText(requireContext(),"Ruta agregada a publicaiones correctamente", Toast.LENGTH_SHORT).show()

        }

            .addOnFailureListener { exception ->

            }

    }


    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {

            val intent = result.data

            val imageBitmap = intent?.extras?.get("data") as Bitmap

            binding.iViewFotoSeleccionnada.setImageBitmap(imageBitmap)

        }

    }

    companion object {
        private const val REQUEST_CODE_LOCATION = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.fragment_container) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        mMap!!.setOnMyLocationButtonClickListener(this)

        mMap!!.setOnMyLocationClickListener(this)

        val madrid = LatLng(40.416775, -3.703790)

        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid, 10f))

        enableMyLocation()

    }

    override fun onMyLocationButtonClick(): Boolean {

        return false

    }

    override fun onMyLocationClick(p0: android.location.Location) {

    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(contextWrapper, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            mMap.isMyLocationEnabled = true

            getLastLocation()

        } else {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)

        }

    }

    private fun getLastLocation() {

        if (ActivityCompat.checkSelfPermission(contextWrapper, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contextWrapper, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(requireActivity(), arrayOf( Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_LOCATION)

            return

        }

        fusedLocationClient.lastLocation

            .addOnSuccessListener { location ->

                if (location != null) {

                    currentLatLng = LatLng(location.latitude, location.longitude)

                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))

                }

            }

            .addOnFailureListener { e ->

                Toast.makeText(contextWrapper,"Error getting location: ${e.message}",Toast.LENGTH_SHORT).show()

            }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == REQUEST_CODE_LOCATION) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                enableMyLocation()

            }

        }

    }

    private fun insertarDatos(nombreUsuario: String, lugares: List<Map<String, String>>) {

        val collectionRef = Firebase.firestore.collection("favorito")

        val datosDocumento = hashMapOf("nombreUsuario" to nombreUsuario, "lugares" to lugares.map { lugar ->
            hashMapOf<String, Any>("nombre" to (lugar["NOMBRE"] ?: ""),
                "latitud" to (lugar["LATITUD"] ?: ""),
                "longitud" to (lugar["LONGITUD"] ?: "")) })

        collectionRef.add(datosDocumento).addOnSuccessListener {

            Toast.makeText(requireContext(),"Ruta agregada a favoritos correctamente", Toast.LENGTH_SHORT).show()

        }

            .addOnFailureListener { exception ->

            }

    }

    private fun shareRoute(randomDataList: List<Map<String, String>>) {

        val shareIntent = Intent(Intent.ACTION_SEND)

        shareIntent.type = "text/plain"

        val routeDescription = "¡Echa un vistazo a esta ruta que he generado en TripTrendy!\n\n"

        val lugares = StringBuilder()

        val optimizedRoute = findOptimizedRoute(randomDataList)

        optimizedRoute.forEachIndexed { index, latLng ->

            val lugar = randomDataList[index]["NOMBRE"] ?: "Lugar Desconocido"

            val latitud = randomDataList[index]["LATITUD"] ?: "Latitud Desconocida"

            val longitud = randomDataList[index]["LONGITUD"] ?: "Longitud Desconocida"

            lugares.append("Lugar ${index + 1}: $lugar\n")

            lugares.append("Ubicación: Latitud $latitud, Longitud $longitud\n\n")

        }

        val routeDetails = "$routeDescription\n$lugares"

        shareIntent.putExtra(Intent.EXTRA_TEXT, routeDetails)

        startActivity(Intent.createChooser(shareIntent, "Compartir ruta a través de:"))

    }

    private var alertDialog: AlertDialog? = null

    private fun showSelectedLocations() {

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setTitle("Sitios Seleccionados")

        val scrollView = ScrollView(requireContext())

        val linearLayout = LinearLayout(requireContext())

        linearLayout.orientation = LinearLayout.VERTICAL

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins(32, 16, 32, 16)

        linearLayout.layoutParams = layoutParams

        lugaresSeleccionados.forEachIndexed { index, lugar ->

            val textView = TextView(requireContext())

            textView.text = lugar["NOMBRE"]

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)

            textView.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)

            textView.setPadding(16, 8, 16, 8)

            linearLayout.addView(textView)

            val button = Button(requireContext())

            button.text = "Eliminar"

            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17F)

            button.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)

            button.setTextColor(Color.RED)

            button.background = getRoundedButtonDrawable()

            button.setPadding(16, 8, 16, 8)

            button.setOnClickListener {

                lugaresSeleccionados.removeAt(index)

                todosLosLugares.add(lugar)

                displayRandomData()

                showSelectedLocations()

            }

            linearLayout.addView(button)

        }

        val separator = View(requireContext())

        val separatorParams = LinearLayout.LayoutParams(

            LinearLayout.LayoutParams.MATCH_PARENT,

            resources.getDimensionPixelSize(R.dimen.separator_height))

        separatorParams.bottomMargin = 16

        separator.layoutParams = separatorParams

        separator.setBackgroundColor(Color.GRAY)

        linearLayout.addView(separator)

        val generarRutaButton = Button(requireContext())

        generarRutaButton.text = "Generar Ruta"

        val generarRutaLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        generarRutaLayoutParams.setMargins(32, 16, 32, 16)

        generarRutaButton.layoutParams = generarRutaLayoutParams

        generarRutaButton.setTextColor(Color.BLACK)

        generarRutaButton.background = getRoundedButtonDrawable()

        generarRutaButton.setPadding(32, 16, 32, 16)

        generarRutaButton.setOnClickListener {

            alertDialog?.dismiss()

            findAndDrawOptimizedRoute()

            binding.fragmentContainer.visibility = View.VISIBLE

        }

        linearLayout.addView(generarRutaButton)

        scrollView.addView(linearLayout)

        alertDialogBuilder.setView(scrollView)

        alertDialog = alertDialogBuilder.create()

        alertDialog?.show()

    }

    private fun findAndDrawOptimizedRoute() {

        val optimizedRoute = findOptimizedRoute(lugaresSeleccionados)

        addMarkersAndDrawRoute(lugaresSeleccionados, optimizedRoute)

    }

    private fun getRoundedButtonDrawable(): Drawable {

        val shapeDrawable = GradientDrawable()

        shapeDrawable.shape = GradientDrawable.RECTANGLE

        shapeDrawable.cornerRadius = 20F

        shapeDrawable.setColor(Color.parseColor("#EAE5E5"))

        return shapeDrawable

    }

    private fun fetchRandomData() {

        val urls = listOf(
            "https://datos.madrid.es/egob/catalogo/201132-0-museos.xml",
            "https://datos.madrid.es/egob/catalogo/208844-0-monumentos-edificios.xml",
            //"https://datos.madrid.es/egob/catalogo/209426-0-templos-catolicas.xml",
            //"https://datos.madrid.es/egob/catalogo/209434-0-templos-otros.xml"
            )

        CoroutineScope(Dispatchers.IO).launch {

            val combinedDataList = mutableListOf<Map<String, String>>()

            urls.forEach { url ->
                val data = fetchDataFromUrl(URL(url))
                combinedDataList.addAll(data)
            }

            todosLosLugares.addAll(combinedDataList.shuffled())

            withContext(Dispatchers.Main) {

                displayRandomData()

            }

        }

    }

    private fun fetchDataFromUrl(url: URL): List<Map<String, String>> {

        val connection = url.openConnection()

        connection.connect()

        val inputStream = connection.getInputStream()

        return parseXML(inputStream)

    }

    private fun parseXML(inputStream: InputStream): List<Map<String, String>> {

        val datosList = mutableListOf<Map<String, String>>()

        val factory = XmlPullParserFactory.newInstance()

        val parser = factory.newPullParser()

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

        parser.setInput(inputStream, null)

        var datos = mutableMapOf<String, String>()

        while (parser.next() != XmlPullParser.END_DOCUMENT) {

            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "contenido") {

                datos = mutableMapOf()

            } else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "contenido") {

                datosList.add(datos)

            } else if (parser.eventType == XmlPullParser.START_TAG && parser.name == "atributo") {

                val nombre = parser.getAttributeValue(null, "nombre")

                if (nombre in listOf("NOMBRE", "LATITUD", "LONGITUD")) {

                    datos[nombre] = readText(parser)

                }

            }

        }

        return datosList

    }

    private fun readText(parser: XmlPullParser): String {

        var result = ""

        if (parser.next() == XmlPullParser.TEXT) {

            result = parser.text

            parser.nextTag()

        }

        return result

    }

    private fun displayRandomData(searchQuery: String? = null) {

        val linearLayout = binding.linear

        linearLayout.removeAllViews()

        val lugaresOrdenados = todosLosLugares.sortedByDescending { lugar ->
            contarRepeticiones(lugar["NOMBRE"].toString(), nombreLugar)
        }

        lugaresOrdenados.filter { lugar ->
            searchQuery.isNullOrEmpty() || lugar["NOMBRE"]?.contains(searchQuery, ignoreCase = true) == true
        }.forEachIndexed { index, lugar ->

            val cardViewBinding = ItemRouteCardBinding.inflate(layoutInflater, null, false)

            cardViewBinding.textPlaceName.text = lugar["NOMBRE"]

            val repeticiones = contarRepeticiones(lugar["NOMBRE"].toString(), nombreLugar)

            cardViewBinding.textView3.text = repeticiones.toString()

            cardViewBinding.btnAAdir.setOnClickListener {

                lugaresSeleccionados.add(lugar)

                todosLosLugares.remove(lugar)

                displayRandomData()

            }

            linearLayout.addView(cardViewBinding.root)

        }
    }


    private fun contarRepeticiones(nombre: String, lista: List<String>): Int {

        var count = 0

        for (lugar in lista) {

            if (nombre.equals(lugar, ignoreCase = true)) {

                count++

            }

        }

        return count

    }

    private fun findOptimizedRoute(dataList: List<Map<String, String>>): List<LatLng> {

        val latLngList = dataList.map { LatLng(it["LATITUD"]!!.toDouble(), it["LONGITUD"]!!.toDouble()) }

        val sortedIndices = mutableListOf<Int>()

        var nearestIndex = findNearestLocationIndex(latLngList, currentLatLng)

        sortedIndices.add(nearestIndex)

        repeat(latLngList.size - 1) {

            val remainingLocations = latLngList.filterIndexed { index, _ -> index !in sortedIndices }

            nearestIndex = findNearestLocationIndex(remainingLocations, latLngList[sortedIndices.last()])

            sortedIndices.add(latLngList.indexOf(remainingLocations[nearestIndex]))

        }

        val optimizedLatLngList = sortedIndices.map { latLngList[it] }

        return optimizedLatLngList

    }

    private fun findNearestLocationIndex(latLngList: List<LatLng>, origin: LatLng): Int {

        var nearestIndex = -1

        var minDistance = Double.MAX_VALUE

        latLngList.forEachIndexed { index, destination ->

            val distance = distance(origin, destination)

            if (distance < minDistance) {

                minDistance = distance

                nearestIndex = index

            }

        }

        return nearestIndex
    }

    private fun distance(p1: LatLng, p2: LatLng): Double {

        val lat1 = p1.latitude

        val lon1 = p1.longitude

        val lat2 = p2.latitude

        val lon2 = p2.longitude

        val theta = lon1 - lon2

        var dist = sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) +
                cos(Math.toRadians(lat1))* cos(Math.toRadians(lat2)) * cos(Math.toRadians(theta))

        dist = acos(dist)

        dist = Math.toDegrees(dist)

        dist *= 60 * 1.1515 * 1.609344 * 1000

        return dist

    }

    private fun addMarkersAndDrawRoute(dataList: List<Map<String, String>>, route: List<LatLng>) {

        mMap.clear()

        val lastIndex = route.size - 1

        route.forEachIndexed { index, latLng ->

            val placeName = dataList[index]["NOMBRE"] ?: "Lugar Desconocido"

            val markerOptions = MarkerOptions().position(latLng).title(placeName)

            when (index) {

                0 -> {

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                }

                lastIndex -> {

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                }

                else -> {

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))

                }

            }

            mMap.addMarker(markerOptions)

            if (index < lastIndex) {

                val nextLatLng = route[index + 1]

                mMap.addPolyline(PolylineOptions().add(latLng, nextLatLng).width(5f).color(Color.BLACK))

            }

        }

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    private fun leerDatos(callback: (ArrayList<String>) -> Unit) {
        val nombreLugares: ArrayList<String> = ArrayList()

        val collectionRef = Firebase.firestore.collection("favorito")

        collectionRef.get().addOnSuccessListener { result ->

            for (document in result) {

                val lugares = document.get("lugares") as? List<Map<String, Any>>

                lugares?.forEach { lugar ->

                    val nombre = lugar["nombre"] as? String

                    nombre?.let {

                        nombreLugares.add(it)

                    }

                }

            }

            callback(nombreLugares)


        }.addOnFailureListener { exception ->

            Toast.makeText(requireContext(), "Error al leer los documentos: $exception", Toast.LENGTH_SHORT).show()

            callback(ArrayList())

        }

    }

}
