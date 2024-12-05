package com.example.triptrendyapp.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.triptrendyapp.MainActivityScreen
import com.example.triptrendyapp.MainActivitySignIn
import com.example.triptrendyapp.databinding.ActivityProfileFragmentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private var _binding: ActivityProfileFragmentBinding? = null

    private val binding get() = _binding!!

    private val storage = Firebase.storage

    private val storageRef = storage.reference.child("img_perfil")

    private val db = Firebase.firestore

    private val usuariosCollection = db.collection("usuarios")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ActivityProfileFragmentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val nombreLoginAntiguo = arguments?.getString("usuario").toString()

        var nombreImagenAntiguo: String? = null

        usuarioExiste(nombreLoginAntiguo) { nombreUsuario ->

            nombreUsuario?.let { nombre ->

                binding.eTextNombreUsuario.setText(nombre)

                nombreImagenAntiguo = nombre

                cargarImagenPerfil(nombreImagenAntiguo)

            }

        }

        binding.btnCamara.setOnClickListener {

            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))

        }

        binding.btnGaleria.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            galleryLauncher.launch(intent)

        }

        binding.btnActualizar.setOnClickListener {

            val nombreUsuarioEditText = binding.eTextNombreUsuario.text.toString()

            if (nombreUsuarioEditText.isNotEmpty()) {

                val imageDrawable = binding.iViewFotoSeleccionnada.drawable

                if (imageDrawable != null && imageDrawable is BitmapDrawable) {

                    val imageBitmap = imageDrawable.bitmap

                    val nombreImagen = "$nombreUsuarioEditText.jpg"

                    usuariosCollection.document(nombreUsuarioEditText)
                        .get()
                        .addOnSuccessListener { document ->

                            if (!document.exists()) {

                                crearNuevoUsuario(nombreLoginAntiguo, nombreUsuarioEditText)

                                nombreImagenAntiguo?.let { it1 -> eliminarUsuarioYImagen(nombreLoginAntiguo, it1) }

                                uploadImageToFirebaseStorage(imageBitmap, nombreImagen)

                                Toast.makeText(requireContext(), "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                                Toast.makeText(requireContext(), "Se actualizará al iniciar sesión", Toast.LENGTH_SHORT).show()

                                //actualizarMainActivityScreen(nombreLoginAntiguo, nombreUsuarioEditText)

                            } else {

                                if (nombreUsuarioEditText.equals(nombreImagenAntiguo)) {

                                    nombreImagenAntiguo?.let { it1 -> eliminarUsuarioYImagen(nombreLoginAntiguo, it1) }

                                    crearNuevoUsuario(nombreLoginAntiguo, nombreUsuarioEditText)

                                    uploadImageToFirebaseStorage(imageBitmap, nombreImagen)

                                    Toast.makeText(requireContext(), "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(requireContext(), "Se actualizará al iniciar sesión", Toast.LENGTH_SHORT).show()

                                    //actualizarMainActivityScreen(nombreLoginAntiguo, nombreUsuarioEditText)


                                } else {

                                    Toast.makeText(requireContext(), "El usuario ya existe. Inserte otro nombre.", Toast.LENGTH_SHORT).show()

                                }

                            }

                        }

                        .addOnFailureListener { exception ->

                            Toast.makeText(requireContext(), "Error al verificar la existencia del usuario", Toast.LENGTH_SHORT).show()

                        }

                } else {

                    Toast.makeText(requireContext(), "Por favor, seleccione una imagen de perfil.", Toast.LENGTH_SHORT).show()

                }

            } else {

                Toast.makeText(requireContext(), "Por favor, ingrese un nombre de usuario.", Toast.LENGTH_SHORT).show()

            }

        }

        return root

    }

    private fun actualizarMainActivityScreen(nombreUsuario: String, nombreImagen: String) {

        val intent = Intent(requireActivity(), MainActivityScreen::class.java)

        intent.putExtra("usuario", nombreUsuario)

        intent.putExtra("nombreImagen", nombreImagen)

        startActivity(intent)

    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {

            val data: Intent? = result.data

            val selectedImageUri: Uri? = data?.data

            selectedImageUri?.let { uri ->

                try {

                    val inputStream = requireContext().contentResolver.openInputStream(uri)

                    val bitmap = inputStream?.let { getBitmapFromStream(it) }

                    bitmap?.let {

                        val rotatedBitmap = rotateBitmapIfRequired(it, uri)

                        binding.iViewFotoSeleccionnada.setImageBitmap(rotatedBitmap)

                    }

                    inputStream?.close()

                } catch (e: Exception) {

                    Log.e("ProfileFragment", "Error al cargar la imagen: $e")

                    Toast.makeText(requireContext(), "Error al cargar la imagen", Toast.LENGTH_SHORT).show()

                }

            }

        }

    }

    private fun getBitmapFromStream(inputStream: InputStream): Bitmap {

        return BitmapFactory.decodeStream(inputStream)

    }

    private fun rotateBitmapIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {

        val input = requireContext().contentResolver.openInputStream(uri)

        val exif = input?.let { ExifInterface(it) }

        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            else -> bitmap
        }

    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {

        val matrix = Matrix()

        matrix.postRotate(angle.toFloat())

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)

    }


    private fun cargarImagenPerfil(nombreImagen: String?) {
        nombreImagen ?: return

        val nombreConExtension = if (!nombreImagen.toLowerCase().endsWith(".jpg")) "$nombreImagen.jpg" else nombreImagen

        val carpetaImagenes = "img_perfil"

        val storageReference = FirebaseStorage.getInstance().getReference().child(carpetaImagenes).child(nombreConExtension)

        storageReference.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->

                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                binding.iViewFotoSeleccionnada.setImageBitmap(bitmap)

            }

            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Error al descargar imagen: $exception")
            }
    }


    private fun usuarioExiste(nombreComprobar: String, callback: (String?) -> Unit) {

        val collectionRef = Firebase.firestore.collection("usuarios")

        collectionRef.whereEqualTo("nombreLogin", nombreComprobar)

            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty) {

                    val nombreUsuario = result.documents[0].getString("nombre")

                    callback(nombreUsuario)

                } else {

                    callback(null)

                }

            }
            .addOnFailureListener { exception ->

                callback(null)

            }

    }

    private fun crearNuevoUsuario(nombreLogin: String, nombreUsuario: String) {

        val usuarioData = hashMapOf("nombreLogin" to nombreLogin, "nombre" to nombreUsuario)

        usuariosCollection.document(nombreUsuario)
            .set(usuarioData)
            .addOnSuccessListener {}
            .addOnFailureListener { e -> }

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        if (result.resultCode == Activity.RESULT_OK) {

            val intent = result.data

            val imageBitmap = intent?.extras?.get("data") as Bitmap

            binding.iViewFotoSeleccionnada.setImageBitmap(imageBitmap)

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

                Log.d("ProfileFragment", "Imagen subida exitosamente")

            } else {

                Log.e("ProfileFragment", "Error al subir la imagen: ${task.exception}")

            }

        }

    }

    private fun eliminarUsuarioYImagen(nombreLogin: String, nombreUsuario: String) {

        val imagenRef = storageRef.child("$nombreUsuario.jpg")

        imagenRef.delete()

            .addOnSuccessListener {

                Log.d("ProfileFragment", "Imagen eliminada exitosamente")

            }

            .addOnFailureListener { e ->

                Log.e("ProfileFragment", "Error al eliminar la imagen: $e")

            }

        usuariosCollection.document(nombreUsuario)
            .delete()
            .addOnSuccessListener {

                Log.d("ProfileFragment", "Usuario eliminado exitosamente")

            }

            .addOnFailureListener { e ->

                Log.e("ProfileFragment", "Error al eliminar el usuario: $e")

            }

    }

}
