package com.partyhub.feature.history

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.partyhub.PartyHubApp
import com.partyhub.R
import com.partyhub.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as PartyHubApp
        HistoryViewModelFactory(HistoryRepository(app.database.matchDao()))
    }

    private val adapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.rvHistory.adapter = adapter

        viewModel.allMatches.observe(viewLifecycleOwner) { matches ->
            if (matches.isEmpty()) {
                binding.tvEmptyHistory.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvEmptyHistory.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                adapter.setMatches(matches)
            }
        }

        viewModel.syncWithCloud()

        setupMenu()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_history -> {
                        showDeleteConfirmation()
                        true
                    }
                    R.id.action_share -> {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, getString(R.string.history_share_text))
                        }
                        startActivity(Intent.createChooser(intent, null))
                        true
                    }
                    R.id.action_logout -> {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                        findNavController().navigate(R.id.authFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.history_delete_title))
            .setMessage(getString(R.string.history_delete_message))
            .setNegativeButton(getString(R.string.history_delete_cancel), null)
            .setPositiveButton(getString(R.string.history_delete_confirm)) { _, _ ->
                viewModel.deleteAll()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
