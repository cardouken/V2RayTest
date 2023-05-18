package com.miljana.v2raytest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.miljana.v2ray.V2RayRepositoryImpl
import com.miljana.v2raytest.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    private var isConnected = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val v2RayRepositoryImpl = V2RayRepositoryImpl()
        v2RayRepositoryImpl.init(requireContext())

        binding.buttonFirst.setOnClickListener {
            if (isConnected) {
                v2RayRepositoryImpl.stopV2Ray(requireContext())
            } else {
                v2RayRepositoryImpl.startV2Ray(requireContext())
            }
            isConnected = !isConnected
            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}