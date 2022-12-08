package com.graffity.arcloud.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.graffity.android.arcloud.databinding.FragmentErrorLandingBinding

internal class ErrorLandingFragment : Fragment() {
    // (private val errorDesc: String?)

    private var _binding: FragmentErrorLandingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentErrorLandingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.errorLandingTextViewDesc
        textView.text = "Something wrong please try again later"



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}