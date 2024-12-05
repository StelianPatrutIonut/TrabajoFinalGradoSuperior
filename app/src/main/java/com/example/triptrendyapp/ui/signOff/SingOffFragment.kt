package com.example.triptrendyapp.ui.signOff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.content.Intent
import com.example.triptrendyapp.MainActivitySignIn
import com.example.triptrendyapp.databinding.ActivitySignoffFragmentBinding

class SingOffFragment : Fragment(){

    private var _binding: ActivitySignoffFragmentBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = ActivitySignoffFragmentBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val intent = Intent(requireActivity(), MainActivitySignIn::class.java)

        startActivity(intent)

        return root

    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null

    }

}


