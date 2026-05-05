package com.partyhub.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.partyhub.R
import com.partyhub.databinding.FragmentAuthBinding

/**
 * Fragmento encargado de la interfaz de inicio de sesión y registro.
 */
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateFields(email, password)) {
                viewModel.signIn(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateFields(email, password)) {
                viewModel.signUp(email, password)
            }
        }
    }

    private fun setupObservers() {
        // Observar el usuario para navegar al Hub cuando se loguee
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                findNavController().navigate(R.id.action_authFragment_to_hubFragment)
            }
        }

        // Observar errores
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Observar estado de carga para mostrar el ProgressBar
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading
        }
    }

    private fun validateFields(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "El email no puede estar vacío"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return false
        }
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
