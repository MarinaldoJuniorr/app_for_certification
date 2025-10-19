package com.example.app_for_certification.presentation.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.Locale

class CountryDetailFragment : Fragment() {

    private var _binding: FragmentCountryDetailBinding? = null
    private val binding get() = _binding!!

    private var hasShownOfflineHint = false

    private val viewModel: CountryDetailViewModel by viewModels {
        val api: Api = RetrofitClient.retrofitInstance.create(Api::class.java)

        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java,
            "countries.db"
        ).build()

        val repo: CountryRepositoryDomain = CountryRepositoryImpl(api, db)
        val useCases = CodeUseCases(repo)

        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CountryDetailViewModel(useCases) as T
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

        viewModel.state.observe(viewLifecycleOwner) { state ->
            render(state)

            if (!hasShownOfflineHint &&
                state is CountryUiState.Error &&
                state.offline
            ) {
                hasShownOfflineHint = true
                showOfflineSnackbar(code)
            }

            if (!hasShownOfflineHint &&
                state is CountryUiState.Success<*> &&
                !isOnline(requireContext())
            ) {
                hasShownOfflineHint = true
                showOfflineSnackbar(code)
            }
        }

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

    private fun showOfflineSnackbar(code: String) {
        Snackbar
            .make(
                requireView(),
                "Offline mode — flags may not load. Check your connection.",
                Snackbar.LENGTH_LONG
            )
            .setAction("Retry") {
                if (code.isNotBlank()) viewModel.load(code)
            }
            .show()
    }

    private fun render(state: CountryUiState<CountryDomain>) {
        binding.viewLoading.root.isVisible = state is CountryUiState.Loading
        binding.viewError.root.isVisible = state is CountryUiState.Error
        binding.viewEmpty.root.isVisible = state is CountryUiState.Empty

        when (state) {
            is CountryUiState.Success -> bindCountry(state.data)
            is CountryUiState.Error -> {
                binding.viewError.tvError.text = state.message
            }
            else -> Unit
        }
    }

    private fun bindCountry(c: CountryDomain) {
        Glide.with(binding.imgFlagLarge)
            .load(c.flagUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(android.R.color.darker_gray)
            .error(android.R.color.darker_gray)
            // 3) If the image fails to load, this is a good hint that we are offline.
            .listener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<android.graphics.drawable.Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (!hasShownOfflineHint && !isOnline(requireContext())) {
                        hasShownOfflineHint = true
                        showOfflineSnackbar(arguments?.getString("code").orEmpty())
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: Target<android.graphics.drawable.Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean = false
            })
            .into(binding.imgFlagLarge)

        binding.tvCountryName.text = c.name
        binding.tvCapital.text = "Capital: ${c.capital ?: "-"}"
        binding.tvPopulation.text = "Population: ${
            NumberFormat.getInstance(Locale.getDefault()).format(c.population)
        }"
        binding.tvLanguages.text = "Languages: ${c.languages.joinToString()}"
        binding.tvCurrencies.text = "Currencies: ${c.currencies.joinToString()}"
        binding.tvNationality.text = "Nationality: ${c.nationality}"
    }

    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}