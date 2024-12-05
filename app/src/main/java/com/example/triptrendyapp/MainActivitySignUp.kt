package com.example.triptrendyapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.triptrendyapp.databinding.ActivityMainSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class MainActivitySignUp : AppCompatActivity() {
    /**
     * Inicialización tardía del Databinding.
     */

    private lateinit var binding: ActivityMainSignUpBinding

    /**
     * Inicialización tardía del FirebaseAuth.
     */

    private lateinit var firebaseAuth: FirebaseAuth

    /**
     * Inicialización de variable booleana para la visibilidad de la contraseña
     */

    private var esContrasenyaVisible1 = false

    private var esContrasenyaVisible2 = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        /**
         * Inicialización del Databinding.
         */

        binding = ActivityMainSignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)

        /**
         * Instaciación de FirebaseAuth.
         */

        firebaseAuth = FirebaseAuth.getInstance()

        /**
         * Inicialización de componentes.
         */

        val ibtnAtrasMain1: ImageButton = binding.ibtnAtrasMain1

        val eTextCorreoElectronico1: EditText = binding.eTextCorreoElectronico1

        val tViewCorreoElectronico: TextView = binding.tViewCorreoElectronico

        val eTextContrasenya1: EditText = binding.eTextContrasenya1

        val ibtnInfo: ImageButton = binding.ibtnInfo

        val ibtnVisibilidadContrasenya1: ImageButton = binding.ibtnVisibilidadContrasenya1

        val tVIewContrasenya: TextView = binding.tVIewContrasenya

        val eTextContrasenya2: EditText = binding.eTextContrasenya2

        val ibtnVisibilidadContrasenya2: ImageButton = binding.ibtnVisibilidadContrasenya2

        val tViewContrasenya1: TextView = binding.tViewContrasenya1

        val btnCrearCuenta: Button = binding.btnCrearCuenta2

        val btnIniciarSesion2: Button = binding.btnIniciarSesion2

        /**
         * Botón para volver a la activity Main Activity.
         * @see MainActivity
         */

        ibtnAtrasMain1.setOnClickListener {

            startActivity(MainActivity.newIntent(this))

        }

        /**
         * Botón que muestra las reglas de la contraseña
         */

        ibtnInfo.setOnClickListener {

            cuadroDialogoReglasContrasenya()

        }

        /**
         * TextView que sirve en  caso de que la contraseña no sea válida desde aquí se puede
         * mostrar las reglas de la contraseña
         */

        tVIewContrasenya.setOnClickListener {

            cuadroDialogoReglasContrasenya()

        }

        /**
         * Botón visibilidad de la contraseña
         */

        ibtnVisibilidadContrasenya1.setOnClickListener {

            esContrasenyaVisible1 = !esContrasenyaVisible1

            val visibilityDrawable = if (esContrasenyaVisible1) R.drawable.visibility_fill0_wght400_grad0_opsz24 else R.drawable.visibility_off_fill0_wght400_grad0_opsz24

            ibtnVisibilidadContrasenya1.setImageResource(visibilityDrawable)

            val inputType = if (esContrasenyaVisible1) android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            eTextContrasenya1.inputType = inputType

            eTextContrasenya1.setSelection(eTextContrasenya1.text.length)

        }

        /**
         * Botón visibilidad de la contraseña
         */

        ibtnVisibilidadContrasenya2.setOnClickListener {

            esContrasenyaVisible2 = !esContrasenyaVisible2

            val visibilityDrawable = if (esContrasenyaVisible2) R.drawable.visibility_fill0_wght400_grad0_opsz24 else R.drawable.visibility_off_fill0_wght400_grad0_opsz24

            ibtnVisibilidadContrasenya2.setImageResource(visibilityDrawable)

            val inputType = if (esContrasenyaVisible2) android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD else android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            eTextContrasenya2.inputType = inputType

            eTextContrasenya2.setSelection(eTextContrasenya2.text.length)

        }

        /**
         * Botón para crear la cuenta
         */

        btnCrearCuenta.setOnClickListener {

            var esValida = true

            val correoElectronico: String = eTextCorreoElectronico1.text.toString()

            val contrasenya: String = eTextContrasenya1.text.toString()

            val contrasenyaRepetida: String = eTextContrasenya2.text.toString()

            if (correoElectronico.isEmpty()) {

                tViewCorreoElectronico.text = "El correo electrónico esta vacío."

                esValida = false

            } else if (!isValidEmail(correoElectronico)) {

                tViewCorreoElectronico.text  = "El correo electrónico no tiene un formato válido."

            } else {

                tViewCorreoElectronico.text = null

            }

            if (contrasenya.isEmpty()) {

                tVIewContrasenya.text = "La contraseña esta vacía."

                esValida = false

            }else if (contrasenya.length < 8 || !contieneMayuscula(contrasenya) || !contieneMinuscula(contrasenya) || !contieneNumero(contrasenya) || !contieneCaracterEspecial(contrasenya)) {

                tVIewContrasenya.text = "La contraseña no es válida. Haga click aquí para más info."

                esValida = false

            } else {

                tVIewContrasenya.text = null

            }

            if (contrasenyaRepetida.isEmpty()) {

                tViewContrasenya1.text = "La contraseña esta vacía."

                esValida = false

            } else if (contrasenyaRepetida != contrasenya) {

                tViewContrasenya1.text = "Las contraseña no coinciden."

                esValida = false

            } else {

                tViewContrasenya1.text = null

            }

            if (esValida) {

                registrarNuevoUsuario(correoElectronico, contrasenya)

            }


        }

        /**
         * Botón para viaja a la activity Main Activity Sign In.
         * @see MainActivitySignIn
         */

        btnIniciarSesion2.setOnClickListener{

            val intent = Intent(this, MainActivitySignIn::class.java)

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

        fun newIntent(context: Context): Intent {

            return Intent(context, MainActivitySignUp::class.java)

        }

    }

    /**
     * Función para el cuadro de diálogo para las reglas de la contraseña.
     */

    private fun cuadroDialogoReglasContrasenya() {

        val builder = AlertDialog.Builder(this)

        builder.setIcon(R.drawable.info_fill0_wght400_grad0_opsz24)

        builder.setTitle("Reglas de la contraseña")

        builder.setMessage("Al menos 8 caracteres\nAl menos una letra mayúscula\nAl menos un número\nAl menos un carácter especial")

        builder.setPositiveButton("Entendido") { dialog, _ ->

            dialog.dismiss()

        }
        val dialog = builder.create()

        dialog.show()
    }

    /**
     * Función para validar si contiene una minúscula la contraseña.
     */


    private fun contieneMinuscula(password: String): Boolean {

        return password.any { it.isLowerCase() }

    }

    /**
     * Función para validar si contiene una mayúcula la contraseña.
     */

    private fun contieneMayuscula(password: String): Boolean {

        return password.any { it.isUpperCase() }

    }

    /**
     * Función para validar si contiene un númeor la contraseña.
     */

    private fun contieneNumero(password: String): Boolean {

        return password.any { it.isDigit() }

    }

    /**
     * Función para validar si contiene un caracter especial la contraseña.
     */

    private fun contieneCaracterEspecial(password: String): Boolean {

        val regex = Regex("[^A-Za-z0-9 ]")

        return regex.containsMatchIn(password)

    }

    /**
     * Función para registrar un nuevo usuario con correo electrónico y contraseña en Firebase.
     */

    private fun registrarNuevoUsuario(email: String, password: String) {

        firebaseAuth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent)

                }else {

                    val exception = task.exception

                    val mensajeError = if (exception is FirebaseAuthUserCollisionException) "El correo electrónico ya está en uso." else "Error al registrar."

                    binding.tViewCorreoElectronico.text = mensajeError
                }

            }

    }

}