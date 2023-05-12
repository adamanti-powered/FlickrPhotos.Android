package com.mindera.flickergallery.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mindera.flickergallery.adapters.PhotosAdapter
import com.mindera.flickergallery.databinding.ActivityMainBinding
import com.mindera.flickergallery.helpers.Utilities
import com.mindera.flickergallery.model.PhotoToDisplay
import com.mindera.flickergallery.repository.SizesRepository
import com.mindera.flickergallery.viewmodel.SizesViewModel
import network.PhotosRetrofitService
import network.SizesRetrofitService
import repository.PhotosRepository
import viewmodel.PhotosViewModel

class MainActivity : AppCompatActivity() {

    private var _binding:ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val photosRetrofitService = PhotosRetrofitService.getInstance()
    private val sizeRetrofitService = SizesRetrofitService.getInstance()

    lateinit var photosViewModel: PhotosViewModel
    lateinit var sizesViewModel: SizesViewModel
    lateinit var photosAdapter: PhotosAdapter

    private var photosRecyclerView:RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val decorView = window.decorView
        val windowBackground = decorView.background
        Utilities.setupBlurView(this, binding.rootView, windowBackground, binding.blurView, 24F)

        photosViewModel = PhotosViewModel(PhotosRepository(photosRetrofitService))
        sizesViewModel = SizesViewModel(SizesRepository(sizeRetrofitService))

        val photoToDisplay: MutableList<PhotoToDisplay> = mutableListOf<PhotoToDisplay>()

        photosViewModel.photoToDisplayLiveDataList.observe(this) { items ->
            if (items != null) {

                for (item in items) {
                    sizesViewModel.getAllSizes(item.id)
                }
            }
        }

        sizesViewModel.sizesLiveDataList.observe(this) { items ->
            if (items != null) {
                for (item in items) {
                    Log.d(TAG, "Item size type: ${item.label}")

                    if (item.label.equals("Large Square")) {
                        photoToDisplay.add(
                            PhotoToDisplay(
                                label = item.label,
                                title = "",
                                source = item.source
                            )
                        )
                    }
                }

                loadPhotos(photoToDisplay)
            }
        }

        photosViewModel.getAllPhotos()

        photosRecyclerView= binding.photosRecyclerView
        photosRecyclerView?.layoutManager = GridLayoutManager(this, 2)
    }

    fun loadPhotos(items: List<PhotoToDisplay>) {
        photosAdapter = PhotosAdapter(items, this) { item, position ->

            Glide.with(this@MainActivity)
                .load(item.source)
                .into(object : CustomTarget<Drawable?>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable?>?
                    ) {
                        binding.rootView.setBackground(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        photosRecyclerView?.adapter = photosAdapter
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
