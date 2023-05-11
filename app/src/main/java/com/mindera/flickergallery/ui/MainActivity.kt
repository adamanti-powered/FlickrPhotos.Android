package com.mindera.flickergallery.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindera.flickergallery.adapters.PhotosAdapter
import com.mindera.flickergallery.databinding.ActivityMainBinding
import com.mindera.flickergallery.model.Photo
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

        photosViewModel = PhotosViewModel(PhotosRepository(photosRetrofitService))
        sizesViewModel = SizesViewModel(SizesRepository(sizeRetrofitService))

        var photoToDisplay: MutableList<PhotoToDisplay> = mutableListOf<PhotoToDisplay>()

        photosViewModel.photoToDisplayLiveDataList.observe(this, Observer { items ->
            if (items != null) {

                for (item in items) {
                    //Log.d(TAG, "ITEMS: ${item.title}")
                    sizesViewModel.getAllSizes(item.id)
                }
            }
        })

        sizesViewModel.sizesLiveDataList.observe(this, Observer { items ->
            if (items != null) {
                for (item in items) {
                    Log.d(TAG, "ITEMS: ${item.label}")

                    photoToDisplay.add(PhotoToDisplay(
                        id = "",
                        label = item.label,
                        title = "",
                        url = item.url
                    ))
                }

                loadPhotos(photoToDisplay)
            }
        })

        photosViewModel.getAllPhotos()

        photosRecyclerView= binding.photosRecyclerView
        photosRecyclerView?.layoutManager = GridLayoutManager(this, 2)
    }

    fun loadPhotos(items: List<PhotoToDisplay>) {
        photosAdapter = PhotosAdapter(items, this, { item, position ->
            // item...
        })

        photosRecyclerView?.adapter = photosAdapter
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
