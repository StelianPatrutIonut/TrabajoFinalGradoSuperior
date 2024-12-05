package com.example.triptrendyapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.triptrendyapp.databinding.ActivityMainRegistroBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class MainActivityRegistro : AppCompatActivity() {

    private lateinit var binding: ActivityMainRegistroBinding

    private val galleryRequestCode = 456

    private val storage = Firebase.storage

    private val storageRef = storage.reference.child("img_perfil")

    private val db = Firebase.firestore

    private val usuariosCollection = db.collection("usuarios")

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainRegistroBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.ibtnAtrasInicioSesion.setOnClickListener {

            val intent = Intent(this, MainActivitySignIn::class.java)

            startActivity(intent)

        }

        val nombreLogin = intent.getStringExtra("usuario")

        binding.btnCamara.setOnClickListener {

            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

        }

        binding.btnGaleria.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(intent, galleryRequestCode)

        }

        binding.btnRegistro.setOnClickListener {

            val nombreUsuario = binding.eTextNombreUsuario.text.toString()

            val imagenSeleccionada = binding.iViewFotoSeleccionnada.drawable

            if (nombreUsuario.isEmpty()) {


                Toast.makeText(this, "Por favor, ingrese un nombre de usuario.", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            }

            if (imagenSeleccionada == null) {

                Toast.makeText(this, "Por favor, seleccione una imagen de perfil.", Toast.LENGTH_SHORT).show()

                return@setOnClickListener

            }

            usuariosCollection.document(nombreUsuario)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {

                        Toast.makeText(this, "El usuario ya existe. Inserte otro nombre.", Toast.LENGTH_SHORT).show()

                        return@addOnSuccessListener

                    } else {

                        if (nombreLogin != null) {

                            crearNuevoUsuario(nombreLogin, nombreUsuario)

                            iniciarSiguienteActividad(nombreUsuario, nombreLogin)

                            val nombreImagen = "${nombreUsuario}.jpg"

                            val imageBitmap = (imagenSeleccionada as BitmapDrawable).bitmap

                            uploadImageToFirebaseStorage(imageBitmap, nombreImagen)

                            binding.eTextNombreUsuario.setText("")

                            binding.iViewFotoSeleccionnada.setImageBitmap(null)

                        }

                    }

                }

                .addOnFailureListener { e ->

                    Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show()

                }

        }

    }


    private fun getCorrectlyOrientedImage(uri: Uri): Bitmap {

        val inputStream = contentResolver.openInputStream(uri)

        val exif = inputStream?.use { input ->

            ExifInterface(input)

        }
        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    }

    private fun crearNuevoUsuario(nombreLogin: String, nombreUsuario: String) {

        val usuarioData = hashMapOf("nombreLogin" to nombreLogin, "nombre" to nombreUsuario)

        usuariosCollection.document(nombreUsuario)
            .set(usuarioData)
            .addOnSuccessListener {

                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

           }

            .addOnFailureListener { e ->


                Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()

            }

    }

    private fun iniciarSiguienteActividad(nombreUsuario: String, nombreLogin: String) {

        val intent = Intent(this@MainActivityRegistro, MainActivityScreen::class.java)

        intent.putExtra("nombreImagen", nombreUsuario)

        intent.putExtra("usuario", nombreLogin)

        startActivity(intent)

        finish()

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {

            val intent = result.data

            val imageBitmap = intent?.extras?.get("data") as Bitmap

            binding.iViewFotoSeleccionnada.setImageBitmap(imageBitmap)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == galleryRequestCode && resultCode == Activity.RESULT_OK && data != null) {

            val selectedImage: Uri? = data.data

            selectedImage?.let {

                val imageBitmap = getCorrectlyOrientedImage(it)

                binding.iViewFotoSeleccionnada.setImageBitmap(imageBitmap)

                val nombreImagen = "${binding.eTextNombreUsuario.text.toString()}.jpg"

                uploadImageToFirebaseStorage(imageBitmap, nombreImagen)

            }

        }

    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, nombreImagen: String) {

        val imageRef = storageRef.child(nombreImagen)

        val baos = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

        val data = baos.toByteArray()

        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Log.d("MainActivity", "Imagen subida exitosamente")

            } else {

                Log.e("MainActivity", "Error al subir la imagen: ${task.exception}")

            }

        }

    }

}
