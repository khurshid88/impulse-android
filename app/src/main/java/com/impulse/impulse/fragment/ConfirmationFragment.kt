package com.impulse.impulse.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.impulse.impulse.R
import com.impulse.impulse.databinding.FragmentConfirmationBinding
import com.impulse.impulse.databinding.FragmentPhoneNumberBinding

class ConfirmationFragment : BaseFragment() {
    private var _binding: FragmentConfirmationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfirmationBinding.inflate(inflater, container, false)
        val view = binding.root
        initViews()
        return view
    }

    private fun initViews() {

    }


}