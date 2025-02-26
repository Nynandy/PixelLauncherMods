package com.kieronquinn.app.pixellaunchermods.ui.screens.shortcuts

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.pixellaunchermods.R
import com.kieronquinn.app.pixellaunchermods.databinding.FragmentShortcutsBinding
import com.kieronquinn.app.pixellaunchermods.ui.base.BoundFragment
import com.kieronquinn.app.pixellaunchermods.ui.base.CanShowSnackbar
import com.kieronquinn.app.pixellaunchermods.utils.extensions.applyBottomNavigationInset
import com.kieronquinn.app.pixellaunchermods.utils.extensions.awaitPost
import com.kieronquinn.app.pixellaunchermods.utils.extensions.isDarkMode
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShortcutsFragment: BoundFragment<FragmentShortcutsBinding>(FragmentShortcutsBinding::inflate), CanShowSnackbar {

    private val viewModel by viewModel<ShortcutsViewModel>()

    private val adapter by lazy {
        ShortcutsAdapter(requireContext(), emptyList(), false, viewModel::onItemClicked).apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateThemedIconsState(requireContext().isDarkMode)
        viewModel.onResume()
    }

    private fun setupRecyclerView() {
        with(binding.shortcutsRecyclerview){
            adapter = this@ShortcutsFragment.adapter
            applyBottomNavigationInset(resources.getDimension(com.kieronquinn.app.pixellaunchermods.R.dimen.margin_16))
        }
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            binding.root.awaitPost()
            binding.shortcutsRecyclerview.layoutManager = createGridLayoutManager()
        }
    }

    private fun createGridLayoutManager(): GridLayoutManager {
        val spanSize = getColumnCount()
        return GridLayoutManager(requireContext(), spanSize)
    }

    private fun getColumnCount(): Int {
        val width = binding.root.measuredWidth
        val columnWidth = resources.getDimension(R.dimen.item_app_width)
        return (width / columnWidth).toInt()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: ShortcutsViewModel.State) {
        binding.shortcutsLoading.isVisible = state is ShortcutsViewModel.State.Loading
        binding.shortcutsRecyclerview.isVisible = state is ShortcutsViewModel.State.Loaded
        binding.shortcutsError.isVisible = state is ShortcutsViewModel.State.Error
        (state as? ShortcutsViewModel.State.Loaded)?.let {
            adapter.items = it.shortcuts
            adapter.themedIconsEnabled = it.themedIconsEnabled
            adapter.notifyDataSetChanged()
        }
    }

}