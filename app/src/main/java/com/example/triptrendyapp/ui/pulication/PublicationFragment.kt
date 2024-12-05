package com.example.triptrendyapp.ui.pulication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
 import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.R
import com.example.triptrendyapp.databinding.ActivityPublicationFragmentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PublicationFragment : Fragment() {

    private var _binding: ActivityPublicationFragmentBinding? = null

    private val binding get() = _binding!!

    private val db = Firebase.firestore

    private val storageRef = Firebase.storage.reference.child("img_chat")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityPublicationFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        db.collection("chat")
            .get()
            .addOnSuccessListener { documents ->

                val chatList = mutableListOf<ChatItem>()

                for (document in documents) {

                    val nombreImagen = document.getString("nombreImagen")

                    val comentario = document.getString("comentario")

                    val lugares = document.get("lugares") as? List<Map<String, Any>>

                    val ruta = document.getString("ruta")

                    val nombreUsuario = document.getString("nombreUsuario")

                    val likes = document.getLong("likes")?.toInt() ?: 0

                    val chatItem = lugares?.let {
                        ChatItem(nombreImagen, comentario,
                            it, nombreUsuario, likes, document.id)
                    }

                    if (chatItem != null) {
                        chatList.add(chatItem)
                    }

                }

                for (chatItem in chatList) {

                    addChatItemToView(chatItem)

                }

            }

            .addOnFailureListener { exception ->

                Log.w("PublicationFragment", "Error obteniendo documentos: ", exception)

            }

        return root

    }

    private fun addChatItemToView(chatItem: ChatItem) {

        val inflater = LayoutInflater.from(context)

        val cardView = inflater.inflate(R.layout.item_publication_card, binding.linear, false)

        val imageView = cardView.findViewById<ImageView>(R.id.imageView)

        val textViewComentario = cardView.findViewById<TextView>(R.id.textViewComentario)

        val textViewRuta = cardView.findViewById<TextView>(R.id.text_place_name)

        val textViewNombreUsuario = cardView.findViewById<TextView>(R.id.textViewNombreUsuario)

        val textViewLikes = cardView.findViewById<TextView>(R.id.textViewLikes)

        val imageViewLike = cardView.findViewById<ImageButton>(R.id.imageButtonLike)


        if (chatItem.nombreImagen != null) {

            val imageRef = storageRef.child(chatItem.nombreImagen)

            imageRef.getBytes(Long.MAX_VALUE)

                .addOnSuccessListener { bytes ->

                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                    imageView.setImageBitmap(bitmap)

                }
                .addOnFailureListener { exception ->

                    Log.e("PublicationFragment", "Error al obtener la imagen: ", exception)

                }

        }

        textViewComentario.text = "Comentario: ${chatItem.comentario}"

        textViewNombreUsuario.text = chatItem.nombreUsuario

        textViewLikes.text = chatItem.likes.toString()

        imageViewLike.setOnClickListener {

            val newLikeCount = chatItem.likes + 1

            db.collection("chat").document(chatItem.nombreDocumento)
                .update("likes", newLikeCount)
                .addOnSuccessListener {

                    Log.d("PublicationFragment", "Número de likes actualizado exitosamente.")

                    textViewLikes.text = newLikeCount.toString()

                }

                .addOnFailureListener { e ->

                    Log.e("PublicationFragment", "Error al actualizar el número de likes: ", e)

                }

        }

        chatItem.lugares.forEachIndexed { index, lugar ->

            val nombre = lugar["nombre"]

            val latitud = lugar["latitud"]

            val longitud = lugar["longitud"]

            textViewRuta.append("Lugar ${index + 1}: $nombre\n\n Latitud: $latitud, Longitud: $longitud\n\n")

        }

        binding.linear.addView(cardView)

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

    data class ChatItem(val nombreImagen: String?, val comentario: String?, val lugares: List<Map<String, Any>>, val nombreUsuario: String?, val likes: Int, val nombreDocumento: String)
}
