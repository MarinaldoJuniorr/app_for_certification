package com.example.app_for_certification.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.app_for_certification.data.local.AppDatabase
import com.example.app_for_certification.data.remote.Api
import com.example.app_for_certification.data.remote.RetrofitClient
import com.example.app_for_certification.data.repository.CountryRepositoryImpl
import com.example.app_for_certification.databinding.FragmentCountryDetailBinding
import com.example.app_for_certification.domain.model.CountryDomain
import com.example.app_for_certification.domain.repository.CountryRepositoryDomain
import com.example.app_for_certification.domain.usercase.CodeUseCases
import com.example.app_for_certification.presentation.model.CountryDetailViewModel
import com.example.app_for_certification.presentation.model.CountryUiState
import java.text.NumberFormat
import java.util.Locale

class CountryDetailFragment : Fragment() {

    private var _binding: FragmentCountryDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CountryDetailViewModel by viewModels {
        val api: Api = RetrofitClient.retrofitInstance.create(Api::class.java)

        val db = Room.databaseBuilder(
            requireContext().applicationContext, AppDatabase::class.java, "countries.db"
        ).build()

        val repo: CountryRepositoryDomain = CountryRepositoryImpl(api, db)
        val useCases = CodeUseCases(repo)

        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST") return CountryDetailViewModel(useCases) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val code = arguments?.getString("code").orEmpty()

        viewModel.state.observe(viewLifecycleOwner) { render(it) }

        if (code.isNotBlank()) {
            viewModel.load(code)
        } else {
            render(CountryUiState.Error("Country code not provided"))
        }

        binding.viewError.btnRetry.setOnClickListener {
            if (code.isNotBlank()) viewModel.load(code)
        }

        binding.ivBackButtonFavorite.setOnClickListener { findNavController().popBackStack() }
        // binding.ivBackButtonUnfavourite.setOnClickListener { findNavController().popBackStack() }
    }

    private fun render(state: CountryUiState<CountryDomain>) {
        binding.viewLoading.root.isVisible = state is CountryUiState.Loading
        binding.viewError.root.isVisible = state is CountryUiState.Error
        binding.viewEmpty.root.isVisible = state is CountryUiState.Empty

        when (state) {
            is CountryUiState.Success -> bindCountry(state.data)
            is CountryUiState.Error -> {
                binding.viewError.tvError.text = state.message
                if (state.offline) {
                    Toast.makeText(
                        requireContext(),
                        "No internet connection — showing cached data if available",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else -> Unit
        }
    }

    private fun bindCountry(c: CountryDomain) {
        Glide.with(binding.imgFlagLarge).load(c.flagUrl).into(binding.imgFlagLarge)
        binding.tvCountryName.text = c.name
        binding.tvCapital.text = "Capital: ${c.capital ?: "-"}"
        binding.tvPopulation.text = "Population: ${
            NumberFormat.getInstance(Locale.getDefault()).format(c.population)
        }"
        binding.tvLanguages.text = "Languages: ${c.languages.joinToString()}"
        binding.tvCurrencies.text = "Currencies: ${c.currencies.joinToString()}"
        binding.tvNationality.text = "Nationality: ${c.nationality}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}