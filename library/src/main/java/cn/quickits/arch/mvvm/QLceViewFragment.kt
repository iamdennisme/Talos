package cn.quickits.arch.mvvm

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import cn.quickits.arch.mvvm.data.ErrorData
import cn.quickits.arch.mvvm.util.LceAnimator
import cn.quickits.arch.mvvm.util.MaterialLceAnimator

abstract class QLceViewFragment<M, VM : QLceViewModel<M>, CV : View> : Fragment() {

    lateinit var viewModel: VM

    lateinit var loadingView: View
    lateinit var contentView: CV
    lateinit var errorView: TextView

    var lceAnimator: LceAnimator = MaterialLceAnimator()

    abstract fun createViewModel(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView = createLoadingView(view)
        contentView = createContentView(view)
        errorView = createErrorView(view)


        viewModel.loader.observe(this, Observer { pullToRefresh ->
            showLoading(pullToRefresh)
            Log.d("QLceViewFragment", "showLoading: $pullToRefresh")
        })

        viewModel.content.observe(this, Observer { content ->
            showContent(content)
            Log.d("QLceViewFragment", "showContent")
        })

        viewModel.error.observe(this, Observer { errorData ->
            showError(errorData)
            Log.d("QLceViewFragment", "showError")
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.load(false)
    }

    protected open fun createLoadingView(view: View): View = view.findViewById(R.id.loading_view)

    protected open fun createContentView(view: View): CV = view.findViewById(R.id.content_view)

    protected open fun createErrorView(view: View): TextView = view.findViewById(R.id.error_view)

    open fun showLoading(pullToRefresh: Boolean?) {
        pullToRefresh ?: return

        if (!pullToRefresh) {
            animateLoadingViewIn()
        }
    }

    open fun showContent(content: M?) {
        animateContentViewIn()
    }

    open fun showError(errorData: ErrorData?) {
        errorData ?: return

        val msg: CharSequence = getErrorMessage(errorData)

        if (errorData.pullToRefresh) {
            showToastError(msg)
        } else {
            errorView.text = msg
            animateErrorViewIn()
        }
    }

    private fun showToastError(msg: CharSequence) {
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    protected open fun getErrorMessage(errorData: ErrorData?): CharSequence {
        errorData ?: return ""
        return errorData.e.message ?: ""
    }

    protected open fun animateLoadingViewIn() {
        lceAnimator.showLoading(loadingView, contentView, errorView)
    }

    protected open fun animateContentViewIn() {
        lceAnimator.showContent(loadingView, contentView, errorView)
    }

    protected open fun animateErrorViewIn() {
        lceAnimator.showErrorView(loadingView, contentView, errorView)
    }

}