package com.darcy.videocutter.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import com.darcy.videocutter.databinding.FragmentViewPagerItemBinding

private const val ARG_TITLE = "title"
private const val ARG_URI_STR = "uri_str"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoThumbnailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoThumbnailFragment : Fragment() {
    private var title: String? = null
    private var uriStr: String? = null
    private var _binding: FragmentViewPagerItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            uriStr = it.getString(ARG_URI_STR)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPagerItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.tvTitle.text = title
        binding.videoImage.setImageURI(uriStr?.toUri())
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String, uriStr: String) =
            VideoThumbnailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_URI_STR, uriStr)
                }
            }
    }
}