package org.xfort.xrockdroid.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 ** Created by ZhangHuaXin on 2021/11/5.
 **/
abstract class XRockFragment<T : ViewBinding> : Fragment() {
    var viewBinding: T? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        viewBinding = createViewBinding(inflater, container, savedInstanceState)
        return viewBinding?.root
    }

    abstract fun createViewBinding(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): T?

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

}