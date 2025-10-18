package com.example.app_for_certification.presentation.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app_for_certification.data.network.Api
import com.example.app_for_certification.data.network.RetrofitClient
import com.example.app_for_certification.data.repository.CountryRepositoryImpl
import com.example.app_for_certification.databinding.FragmentCountryBinding
import com.example.app_for_certification.domain.repository.CountryRepositoryDomain
import com.example.app_for_certification.domain.usercase.CodeUseCases
import com.example.app_for_certification.presentation.model.CountryUiState
import com.example.app_for_certification.presentation.model.CountryViewModel
import com.example.app_for_certification.presentation.ui.adapter.CountryAdapter

class CountryFragment : Fragment() {

    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CountryAdapter

    private val viewModel: CountryViewModel by viewModels {
        val api: Api = RetrofitClient.retrofitInstance.create(Api::class.java)
        val repo: CountryRepositoryDomain = CountryRepositoryImpl(api)
        val useCases = CodeUseCases(repo)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return CountryViewModel(useCases) as T
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CountryAdapter { country ->
            val raw = country.code
            val code = raw.filter { it.isLetter() }.take(3).uppercase()
            Log.d("CountryFragment", "click code raw='$raw' sanitized='$code' name=${country.name}")
            if (code.length in 2..3) {
                val action = CountryFragmentDirections.actionToDetail(code)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Invalid code: '$raw'", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvCountries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCountries.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean { viewModel.search(q.orEmpty()); return true }
            override fun onQueryTextChange(new: String?): Boolean { viewModel.search(new.orEmpty()); return true }
        })

        binding.viewError.btnRetry.setOnClickListener { viewModel.refresh() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CountryUiState.Loading -> {
                    binding.viewLoading.root.visibility = View.VISIBLE
                    binding.viewError.root.visibility = View.GONE
                    binding.viewEmpty.root.visibility = View.GONE
                }
                is CountryUiState.Success -> {
                    binding.viewLoading.root.visibility = View.GONE
                    binding.viewError.root.visibility = View.GONE
                    binding.viewEmpty.root.visibility = View.GONE
                    adapter.submitList(state.data)
                }
                is CountryUiState.Empty -> {
                    binding.viewLoading.root.visibility = View.GONE
                    binding.viewError.root.visibility = View.GONE
                    binding.viewEmpty.root.visibility = View.VISIBLE
                    adapter.submitList(emptyList())
                }
                is CountryUiState.Error -> {
                    binding.viewLoading.root.visibility = View.GONE
                    binding.viewError.root.visibility = View.VISIBLE
                    binding.viewEmpty.root.visibility = View.GONE
                    binding.viewError.tvError.text = state.message
                }
            }
        }

        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView(); _binding = null
    }
}