package org.xfort.xrockdroid.widget

import android.app.Dialog
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.HandlerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import org.xfort.xrockdroid.R

/**
 ** Created by ZhangHuaXin on 2021/7/7.
 **/
class LoadingDialog : AppCompatDialogFragment() {
    val handler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        fun show(fm: FragmentManager, life: Lifecycle?, msg: String?) {
            if (fm.isDestroyed || fm.isStateSaved || life?.currentState?.isAtLeast(Lifecycle.State.STARTED) == false) {
                return
            }
            val fragment = fm.findFragmentByTag("loading")
            if (fragment != null && fragment is LoadingDialog) {
                return
            }
            val loadingDialog = createInstance(msg ?: "")
            loadingDialog.show(fm, "loading")
        }

        fun dismiss(fm: FragmentManager, life: Lifecycle?) {
            if (fm.isDestroyed || life == null || !life.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
                return
            }
            val fragment = fm.findFragmentByTag("loading")
            if (fragment != null && fragment is LoadingDialog) {
                fragment.dismiss()
            }
        }

        fun createInstance(message: String): LoadingDialog {
            val loadingDialog = LoadingDialog()
            loadingDialog.arguments = bundleOf("message" to message)
            return loadingDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.LoadingDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var msg = arguments?.getString("message", null)
        msg = msg ?: "Loading..."
        msg?.let {
            (view.findViewById(R.id.message) as TextView).text = msg
        }

        HandlerCompat.postDelayed(handler, {
            if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                isCancelable = true
            }
        }, "loading", 8000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages("loading")
    }
}