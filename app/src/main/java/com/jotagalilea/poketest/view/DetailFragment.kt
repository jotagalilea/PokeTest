package com.jotagalilea.poketest.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jotagalilea.poketest.R
import com.jotagalilea.poketest.loadImageFromFile
import com.jotagalilea.poketest.loadImageFromUrl
import com.jotagalilea.poketest.model.Pokemon
import com.jotagalilea.poketest.typesToString
import com.jotagalilea.poketest.viewmodel.PokemonViewModel


/**
 * Fragmento de la pantalla de detalle de un pokemon.
 */
class DetailFragment: Fragment() {

	private lateinit var viewModel: PokemonViewModel
	private lateinit var pokemon: Pokemon
	private lateinit var nameText: TextView
	private lateinit var heightText: TextView
	private lateinit var weightText: TextView
	private lateinit var baseExperienceText: TextView
	private lateinit var typesText: TextView
	private lateinit var image: ImageView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		pokemon = arguments?.get("Pokemon") as Pokemon
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.fragment_detail, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		viewModel = (activity as MainActivity).getViewModel()
		setupView(view)
	}

	/**
	 * Configura la vista con la información recibida.
	 * @param view Vista a configurar.
	 */
	private fun setupView(view: View){
		nameText = view.findViewById(R.id.detail_name)
		typesText = view.findViewById(R.id.detail_types)
		heightText = view.findViewById(R.id.detail_height)
		weightText = view.findViewById(R.id.detail_weight)
		baseExperienceText = view.findViewById(R.id.detail_base_experience)
		image = view.findViewById(R.id.detail_image)
		nameText.text = pokemon.name
		baseExperienceText.text = pokemon.data?.value?.baseExperience.toString()
		heightText.text = pokemon.data?.value?.height.toString()
		weightText.text = pokemon.data?.value?.weight.toString()
		typesText.text = pokemon.data?.value?.types?.typesToString()
		setImage()
	}


	/**
	 * Carga la imagen del pokemon seleccionado desde URL o archivo.
	 */
	private fun setImage(){
		var isFromUrl = true
		val path: String = if (pokemon.data?.value?.imageFile.isNullOrEmpty()) {
				pokemon.data?.value?.imageUrl.toString()
			} else {
				isFromUrl = false
				pokemon.data?.value?.imageFile.toString()
			}

		if (isFromUrl)
			context?.let { loadImageFromUrl(pokemon, path, image, it)}
		else
			loadImageFromFile(path, image)
	}

}