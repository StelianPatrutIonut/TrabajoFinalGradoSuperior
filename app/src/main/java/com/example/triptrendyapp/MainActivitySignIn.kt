package com.example.triptrendyapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.triptrendyapp.databinding.ActivityMainSignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivitySignIn : AppCompatActivity() {
    /**
     * Inicialización tardía del Databinding.
     */

    private lateinit var binding: ActivityMainSignInBinding

    /**
     * Inicialización tardía del FirebaseAuth.
     */

    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * Inicialización de variable booleana para la visibilidad de la contraseña.
     */

    private var esContrasenyaVisible = false

    /**
     * Inicialización de variable con el código de solicitud para identificar la solicitud de
     * inicio de sesión utilizando Firebase Authentication.
     */

    private val RC_SIGN_IN = 123

    /**
     * Inicialización de variable para interactuar con Google Sign-In API.
     */

    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /**
         * Inicialización del Databinding.
         */

        binding = ActivityMainSignInBinding.inflate(layoutInflater)

        setContentView(binding.root)

        /**
         * Instaciación de FirebaseAuth.
         */

        firebaseAuth = FirebaseAuth.getInstance()

        /**
         * Inicialización de componentes.
         */

        val ibtnAtrasMain: ImageButton = binding.ibtnAtrasMain

        val btnCrearCuenta1: Button = binding.btnCrearCuenta1

        val eTextCorreoElectronico: EditText = binding.eTextCorreoElectronico

        val eTextContrasenya: EditText = binding.eTextContrasenya

        val ibtnVisibilidadContrasenya: ImageButton = binding.ibtnVisibilidadContrasenya

        val tViewCorreoContrasenya: TextView = binding.tViewCorreoContrasenya

        val btnRecuperarContrasenya: Button = binding.btnRecuperarContrasenya

        val btnIniciarSesion1: Button = binding.btnIniciarSesion1

        val btnContinuaGoogle: Button = binding.btnContinuaGoogle

        val btnContinuaTelefono: Button = binding.btnContinuaTelefono


        /**
         * Botón para volver a la activity Main Activity.
         * @see MainActivity
         */

        ibtnAtrasMain.setOnClickListener {

            startActivity(MainActivity.newIntent(this))

        }

        /**
         * Botón para viajar a la activity Main Activity Sign Up.
         * @see MainActivitySignUp
         */

        btnCrearCuenta1.setOnClickListener {

            startActivity(MainActivitySignUp.newIntent(this))

        }

        /**
         * Botón para iniciar sesión con correo electrónico y contraseña.
         */

        btnIniciarSesion1.setOnClickListener {

            val correoElectronico = eTextCorreoElectronico.text.toString()

            val contrasenya = eTextContrasenya.text.toString()

            if (correoElectronico.isEmpty() || contrasenya.isEmpty()) {

                tViewCorreoContrasenya.text = "El correo electrónico o la contraseña está vacío."

            } else if (!isValidEmail(correoElectronico)) {

                tViewCorreoContrasenya.text = "El correo electrónico no tiene un formato válido."

            } else {

                tViewCorreoContrasenya.text = null

                loginEmailAndPassword(correoElectronico, contrasenya)

            }

        }

        /**
         * Botón visibilizar la contraseña.
         */

        ibtnVisibilidadContrasenya.setOnClickListener {

            esContrasenyaVisible = !esContrasenyaVisible

            val visibilityDrawable = if (esContrasenyaVisible) R.drawable.visibility_fill0_wght400_grad0_opsz24 else R.drawable.visibility_off_fill0_wght400_grad0_opsz24

            ibtnVisibilidadContrasenya.setImageResource(visibilityDrawable)

            val inputType = if (esContrasenyaVisible) android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            eTextContrasenya.inputType = inputType

            eTextContrasenya.setSelection(eTextContrasenya.text.length)

        }

        /**
         * Botón para ver el cuadro de diálogo para cambiar la contraseña.
         */

        btnRecuperarContrasenya.setOnClickListener {

            cuadroDialogoRecuperarContrasenya()

        }

        /**
         * Botón para iniciar sesión con Google.
         */

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

            .requestIdToken(getString(R.string.default_web_client_id))

            .requestEmail()

            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnContinuaGoogle.setOnClickListener {

            signInWithGoogle()

        }

        /**
         * Botón para iniciar sesión con el número de teléfono.
         */

        btnContinuaTelefono.setOnClickListener {

            val intent = Intent(this, MainActivitySignInPhone::class.java)

            startActivity(intent)

        }

    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


    /**
     * Este objeto se utiliza para navegar de una activity a esta activity.
     */

    companion object {

        fun newIntent(context: MainActivitySignInPhone): Intent {

            return Intent(context, MainActivitySignIn::class.java)

        }

    }

    /**
     * Función para el inicio de sesión con correo electrónico y contraseña.
     */

    private fun loginEmailAndPassword(email: String, password: String) {

        firebaseAuth.signInWithEmailAndPassword(email, password)

            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    binding.tViewCorreoContrasenya.text = null

                    Toast.makeText(this, "Inicio de Sesión completado", Toast.LENGTH_SHORT).show()

                    usuarioExiste(email) { usuarioExiste, nombreUsuario ->

                        if (!usuarioExiste) {

                            val intent = Intent(this, MainActivityRegistro::class.java)

                            intent.putExtra("usuario", email)

                            startActivity(intent)

                            finish()

                        } else {

                            val intent = Intent(this, MainActivityScreen::class.java)

                            intent.putExtra("usuario", email)

                            intent.putExtra("nombreImagen", nombreUsuario)

                            startActivity(intent)

                            finish()

                        }

                    }

                } else {

                    val exception = task.exception

                    val mensajeError = when (exception) {

                        is FirebaseAuthInvalidCredentialsException -> "Usuario o contraseña es inválido."

                        is FirebaseNetworkException -> "Error de red."

                        else -> "Error desconocido."

                    }

                    binding.tViewCorreoContrasenya.text = mensajeError

                }

            }

    }

    /**
     * Función para el cuadro de diálogo para el cambio de contraseña.
     */

    private fun cuadroDialogoRecuperarContrasenya() {

        val builder = AlertDialog.Builder(this)

        val inflater = layoutInflater

        val dialogLayout = inflater.inflate(R.layout.dialog_cambiar_password, null)

        val eTextCorreoElectronicoCambio = dialogLayout.findViewById<EditText>(R.id.eTextCorreoElectronicoCambio)

        with(builder) {

            setTitle("Cambio de Contraseña")

            setView(dialogLayout)

            setPositiveButton("Aceptar") { _, _ ->

                val correoElectronico = eTextCorreoElectronicoCambio.text.toString()

                if (isValidEmail(correoElectronico)) {

                    cambioContrasenya(correoElectronico)

                } else {

                    Toast.makeText(this@MainActivitySignIn , "Dirección de correo electrónico no válida", Toast.LENGTH_SHORT).show()

                }

            }
            setNegativeButton("Cancelar") { dialog, _ ->

                dialog.cancel()

            }

            show()

        }

    }

    /**
     * Función para el proceso de cambio de contraseña.
     */

    private fun cambioContrasenya(email: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)

            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this, "Se ha enviado un correo electrónico para restablecer la contraseña.", Toast.LENGTH_SHORT).show()

                } else {

                    if (task.exception is FirebaseAuthInvalidUserException) {

                        Toast.makeText(this, "El usuario no se encontró.", Toast.LENGTH_SHORT).show()

                    } else {


                        Toast.makeText(this, "Error al cambiar la contraseña: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                    }

                }

            }
    }

    /**
     * Función del método OnStart para el inicio de sesión con Google.
     */

    public override fun onStart() {

        super.onStart()

        firebaseAuth.currentUser

    }

    /**
     * Método para OnActivity para el inicio de sesion con Google.
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account = task.getResult(ApiException::class.java)!!

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {


                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()

            }

        }

    }

    /**
     * Función para realizar el inicio de sesion con Google.
     */

    private fun signInWithGoogle() {

        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    /**
     * Función para realizar la autentificación de Google con Firebase.
     */

    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = firebaseAuth.currentUser

                    val userEmail = user?.email

                    if (userEmail != null) {

                        Toast.makeText(this, "Inicio de Sesión completado", Toast.LENGTH_SHORT).show()

                        usuarioExiste(userEmail) { usuarioExiste, nombreUsuario ->

                            if (!usuarioExiste) {

                                val intent = Intent(this, MainActivityRegistro::class.java)

                                intent.putExtra("usuario", userEmail)

                                startActivity(intent)

                                googleSignInClient.revokeAccess()

                                finish()

                            } else {

                                val intent = Intent(this, MainActivityScreen::class.java)

                                intent.putExtra("usuario", userEmail)

                                intent.putExtra("nombreImagen", nombreUsuario)

                                startActivity(intent)

                                googleSignInClient.revokeAccess()

                                finish()

                            }

                        }

                    }

                }

            }

    }

    /**
     * Función para realizar la comprobación de que el usuario esta registrado.
     */

    private fun usuarioExiste(nombreComprobar: String, callback: (Boolean, String?) -> Unit) {

        val collectionRef = Firebase.firestore.collection("usuarios")

        collectionRef.whereEqualTo("nombreLogin", nombreComprobar)
            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty) {

                    val nombreUsuario = result.documents[0].getString("nombre")

                    callback(true, nombreUsuario)

                } else {

                    callback(false, null)

                }

            }

            .addOnFailureListener { exception ->

                callback(false, null)

            }

    }

}

