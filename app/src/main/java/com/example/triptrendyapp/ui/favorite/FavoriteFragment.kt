package com.example.triptrendyapp.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.R
import com.example.triptrendyapp.databinding.ActivityFavoriteFragmentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FavoriteFragment : Fragment() {

    private var _binding: ActivityFavoriteFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var nombreUsuario: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = ActivityFavoriteFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        nombreUsuario = arguments?.getString("usuario").toString()

        leerDatos()

        binding.btnElimarRutas.setOnClickListener {

            eliminarTodasLasRutas()

        }

        return root

    }

    override fun onStop() {

        super.onStop()

        _binding = null

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    private fun leerDatos() {

        val collectionRef = Firebase.firestore.collection("favorito")

        collectionRef.whereEqualTo("nombreUsuario", nombreUsuario).get().addOnSuccessListener { result ->

            val linearLayout = binding.linear

            linearLayout.removeAllViews()

            var routeNumber = 1

            for (document in result) {

                val lugares = document.get("lugares") as? List<Map<String, Any>>

                val cardView = layoutInflater.inflate(R.layout.item_favorite_card, null)

                val textPlaceName = cardView.findViewById<TextView>(R.id.text_place_name)

                val btnDelete = cardView.findViewById<Button>(R.id.btn_añadir)

                val stringBuilder = StringBuilder()

                stringBuilder.append("Ruta $routeNumber:\n\n")

                lugares?.forEachIndexed { index, lugar ->

                    val nombre = lugar["nombre"]

                    val latitud = lugar["latitud"]

                    val longitud = lugar["longitud"]

                    stringBuilder.append("Lugar ${index + 1}: $nombre\n")

                    stringBuilder.append("Latitud: $latitud, Longitud: $longitud\n\n")

                }

                routeNumber++

                textPlaceName.text = stringBuilder.toString()

                linearLayout.addView(cardView)

                btnDelete.setOnClickListener {

                    eliminarRuta(document.id)

                }

            }

        }.addOnFailureListener { exception ->

            Toast.makeText(requireContext(), "Error al leer los documentos: $exception", Toast.LENGTH_SHORT).show()

        }

    }

    private fun eliminarRuta(documentId: String) {

        val documentRef = Firebase.firestore.collection("favorito").document(documentId)

        documentRef.delete()

            .addOnSuccessListener {

                Toast.makeText(requireContext(), "Ruta eliminada correctamente", Toast.LENGTH_SHORT).show()

                leerDatos()

            }

            .addOnFailureListener { exception ->

                Toast.makeText(requireContext(), "Error al eliminar la ruta: $exception", Toast.LENGTH_SHORT).show()

            }

    }

    private fun eliminarTodasLasRutas() {

        val collectionRef = Firebase.firestore.collection("favorito")

        collectionRef.whereEqualTo("nombreUsuario", nombreUsuario)

            .get()

            .addOnSuccessListener { result ->

                for (document in result) {

                    document.reference.delete()

                }

                leerDatos()

                Toast.makeText(requireContext(), "Todas las rutas eliminadas correctamente", Toast.LENGTH_SHORT).show()

            }

            .addOnFailureListener { exception ->

                Toast.makeText(requireContext(), "Error al eliminar todas las rutas: $exception", Toast.LENGTH_SHORT).show()

            }

    }

}
