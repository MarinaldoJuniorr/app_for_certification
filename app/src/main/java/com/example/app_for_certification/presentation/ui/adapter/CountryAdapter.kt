package com.example.app_for_certification.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app_for_certification.databinding.ItemCountryBinding
import com.example.app_for_certification.domain.model.CountryDomain

class CountryAdapter(
    private val onClick: (CountryDomain) -> Unit
) : ListAdapter<CountryDomain, CountryAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CountryDomain>() {
            override fun areItemsTheSame(o: CountryDomain, n: CountryDomain) = o.code == n.code
            override fun areContentsTheSame(o: CountryDomain, n: CountryDomain) = o == n
        }
    }

    inner class VH(private val binding: ItemCountryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CountryDomain) = with(binding) {
            tvName.text = item.name
            tvRegion.text = item.region
            Glide.with(imgFlag).load(item.flagUrl).into(imgFlag)
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}