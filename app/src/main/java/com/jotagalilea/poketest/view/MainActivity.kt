package com.jotagalilea.poketest.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.jotagalilea.poketest.R
import com.jotagalilea.poketest.model.Pokemon
import com.jotagalilea.poketest.viewmodel.PokemonViewModel

/**
 * Actividad principal que contiene el viewModel y el controlador del componente de navegación.
 */
class MainActivity : AppCompatActivity() {

	private lateinit var navController: NavController
	private lateinit var viewModel: PokemonViewModel


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		viewModel = ViewModelProvider(this).get(PokemonViewModel::class.java)
		navController = findNavController(R.id.nav_host)
	}

	/**
	 * Acceso al ViewModel.
	 * @return ViewModel de la activity.
	 */
	fun getViewModel(): PokemonViewModel{
		return viewModel
	}


	/**
	 * Navegación a la pantalla de detalle de un pokemon.
	 */
	fun navigateToDetail(pokemon: Pokemon){
		val bundle = bundleOf("Pokemon" to pokemon)
		navController.navigate(R.id.action_main_to_detail, bundle)
	}

}