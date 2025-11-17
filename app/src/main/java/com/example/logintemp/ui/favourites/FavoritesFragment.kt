package com.example.logintemp.ui.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.widget.addTextChangedListener
import com.example.logintemp.R
import com.example.logintemp.data.AppDatabase
import com.example.logintemp.data.recipe.RecipeRepository
import com.example.logintemp.databinding.FragmentFavoritesBinding
import com.example.logintemp.util.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: FavoriteAdapter
    private lateinit var repo: RecipeRepository
    private lateinit var session: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentFavoritesBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        session = SessionManager(requireContext())
        val db = AppDatabase.getInstance(requireContext())
        repo = RecipeRepository(db.recipeDao())

        adapter = FavoriteAdapter { recipe, pos ->
            // Remove favorite immediately (optimistic)
            removeFavoriteWithUndo(recipe, pos)
        }

        binding.recyclerFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerFavorites.adapter = adapter

        // Observe favorites flow
        viewLifecycleOwner.lifecycleScope.launch {
            repo.getFavoriteRecipesFlow().collectLatest { list ->
                adapter.submitList(list)
                binding.tvNoMore.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }



        // search simple filter
        binding.etSearch.addTextChangedListener { s ->
            val q = s?.toString()?.trim().orEmpty()
            viewLifecycleOwner.lifecycleScope.launch {
                val all = repo.getFavoriteRecipes()
                val filtered = if (q.isEmpty()) all else all.filter { it.name.contains(q, true) }
                adapter.submitList(filtered)
                binding.tvNoMore.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun removeFavoriteWithUndo(recipe: com.example.logintemp.data.recipe.RecipeEntity, position: Int) {
        // store current list to support undo
        val current = adapter.currentList.toMutableList()

        // optimistic removal from UI
        val newList = current.filterNot { it.id == recipe.id }
        adapter.submitList(newList)

        // persist change
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                repo.toggleFavorite(recipe.id, false)
            } catch (_: Exception) {
                // if fail, re-insert into list
                adapter.submitList(current)
                Snackbar.make(binding.root, "Error removing favorite", Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            val sb = Snackbar.make(binding.root, "Removed from favorites", Snackbar.LENGTH_LONG)
            sb.setAction("UNDO") {
                // undo action: set favorite true and restore item in DB & UI
                viewLifecycleOwner.lifecycleScope.launch {
                    repo.toggleFavorite(recipe.id, true)
                    // restore in UI (put at same position if possible)
                    val restored = adapter.currentList.toMutableList()
                    restored.add(position.coerceAtMost(restored.size), recipe)
                    adapter.submitList(restored)
                }
            }
            sb.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}