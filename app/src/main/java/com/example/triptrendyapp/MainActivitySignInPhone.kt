package com.example.triptrendyapp

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.triptrendyapp.databinding.ActivityMainSignInPhoneBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class MainActivitySignInPhone : AppCompatActivity() {

    private lateinit var binding: ActivityMainSignInPhoneBinding

    private lateinit var mAuth: FirebaseAuth

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    private var idVerificacion: String? = null

    private var codigoPaisSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityMainSignInPhoneBinding.inflate(layoutInflater)

        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.ibtnAtrasInicioSesion.setOnClickListener {

            startActivity(MainActivitySignIn.newIntent(this))

        }

        val spinnerPrefijosTelefonicos = binding.spinnerPaises

        val prefijosTelefonicos = arrayOf("+34 (España)", "+1 (Estados Unidos)", "+1 (Canadá)",
            "+52 (México)", "+55 (Brasil)", "+54 (Argentina)", "+57 (Colombia)", "+56 (Chile)",
            "+58 (Venezuela)", "+51 (Perú)", "+593 (Ecuador)", "+53 (Cuba)", "+591 (Bolivia)",
            "+506 (Costa Rica)", "+507 (Panamá)", "+598 (Uruguay)", "+49 (Alemania)", "+33 (Francia)",
            "+39 (Italia)", "+44 (Reino Unido)", "+7 (Rusia)", "+380 (Ucrania)", "+48 (Polonia)",
            "+40 (Rumania)", "+31 (Países Bajos)", "+32 (Bélgica)", "+30 (Grecia)", "+351 (Portugal)",
            "+46 (Suecia)", "+47 (Noruega)", "+86 (China)", "+91 (India)", "+81 (Japón)",
            "+82 (Corea del Sur)", "+62 (Indonesia)", "+90 (Turquía)", "+63 (Filipinas)",
            "+66 (Tailandia)", "+84 (Vietnam)", "+972 (Israel)", "+60 (Malasia)", "+65 (Singapur)",
            "+92 (Pakistán)", "+880 (Bangladés)", "+966 (Arabia Saudita)", "+20 (Egipto)",
            "+27 (Sudáfrica)", "+234 (Nigeria)", "+254 (Kenia)", "+212 (Marruecos)", "+213 (Argelia)",
            "+256 (Uganda)", "+233 (Ghana)", "+237 (Camerún)", "+225 (Costa de Marfil)",
            "+221 (Senegal)", "+255 (Tanzania)", "+249 (Sudán)", "+218 (Libia)", "+216 (Túnez)",
            "+61 (Australia)", "+64 (Nueva Zelanda)", "+679 (Fiji)", "+675 (Papúa Nueva Guinea)",
            "+676 (Tonga)", "+98 (Irán)", "+964 (Iraq)", "+962 (Jordania)", "+961 (Líbano)",
            "+965 (Kuwait)", "+971 (Emiratos Árabes Unidos)", "+968 (Omán)", "+974 (Qatar)",
            "+973 (Bahréin)", "+967 (Yemen)")

        val adaptador = ArrayAdapter(this, R.layout.simple_spinner_item, prefijosTelefonicos)

        adaptador.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerPrefijosTelefonicos.adapter = adaptador

        spinnerPrefijosTelefonicos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                codigoPaisSeleccionado = prefijosTelefonicos[position].substring(0, prefijosTelefonicos[position].indexOf(" "))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.btnObtenerCodigo.setOnClickListener {

            val numeroTelefono = binding.eTextNumeroTelefono.text.toString()

            if (esNumeroTelefonoValido(numeroTelefono)) {

                iniciarVerificacionNumeroTelefono(numeroTelefono)

                binding.eTextNumeroTelefono.isEnabled = false

            } else {

                Toast.makeText(applicationContext, "Introduzca un número de teléfono válido", Toast.LENGTH_LONG).show()

            }

        }

        binding.btnVerificarCodigo.setOnClickListener {

            val codigoVerificacion = binding.eTextCodigoVerificacion.text.toString()

            if (idVerificacion != null && esCodigoVerificacionValido(codigoVerificacion)) {

                verificarNumeroTelefonoConCodigo(idVerificacion!!, codigoVerificacion)

            } else {

                binding.eTextNumeroTelefono.isEnabled = true

                Toast.makeText(applicationContext, "El código de verificación es incorrecto.", Toast.LENGTH_LONG).show()

            }

        }

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                iniciarSesionConCredencialTelefono(credential)

            }

            override fun onVerificationFailed(e: FirebaseException) {

            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

                idVerificacion = verificationId

            }

        }

    }

    private fun esNumeroTelefonoValido(numeroTelefono: String): Boolean {

        return Patterns.PHONE.matcher(numeroTelefono).matches() && numeroTelefono.length == 9 && numeroTelefono.all { it.isDigit() }

    }

    private fun esCodigoVerificacionValido(codigo: String): Boolean {

        return codigo.length == 6 && codigo.all { it.isDigit() }

    }

    private fun iniciarVerificacionNumeroTelefono(numeroTelefono: String) {

        val numeroTelefonoCompleto = "$codigoPaisSeleccionado $numeroTelefono"

        val options = PhoneAuthOptions.newBuilder(mAuth)

            .setPhoneNumber(numeroTelefonoCompleto)

            .setTimeout(60L, TimeUnit.SECONDS)

            .setActivity(this)

            .setCallbacks(mCallbacks)

            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun verificarNumeroTelefonoConCodigo(idVerificacion: String, codigo: String) {

        val credencial = PhoneAuthProvider.getCredential(idVerificacion, codigo)

        iniciarSesionConCredencialTelefono(credencial)

    }

    private fun iniciarSesionConCredencialTelefono(credencial: PhoneAuthCredential) {

        mAuth.signInWithCredential(credencial)

            .addOnCompleteListener(this) { tarea ->

                if (tarea.isSuccessful) {

                    val usuario = tarea.result?.user

                    Toast.makeText(applicationContext, "Inicio de sesión correctamente.", Toast.LENGTH_LONG).show()

                    val numeroTelefono = binding.eTextNumeroTelefono.text.toString()

                    val numeroCompleto = "$codigoPaisSeleccionado $numeroTelefono"

                    usuarioExiste(numeroCompleto) { usuarioExiste, nombreUsuario ->

                        if (!usuarioExiste) {

                            val intent = Intent(this, MainActivityRegistro::class.java)

                            intent.putExtra("usuario", numeroCompleto)

                            startActivity(intent)

                            finish()

                        } else {

                            val intent = Intent(this, MainActivityScreen::class.java)

                            intent.putExtra("usuario", numeroCompleto)

                            intent.putExtra("nombreImagen", nombreUsuario)

                            startActivity(intent)

                            finish()

                        }

                    }


                    val intent = Intent(this, MainActivityRegistro::class.java)

                    intent.putExtra("usuario", numeroCompleto)

                    startActivity(intent)

                    finish()

                } else {

                    Toast.makeText(applicationContext, "El código de verificación es incorrecto.", Toast.LENGTH_LONG).show()

                }

            }
    }

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