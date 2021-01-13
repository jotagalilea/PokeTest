package com.jotagalilea.poketest.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jotagalilea.poketest.R
import com.jotagalilea.poketest.db.PokemonDao
import com.jotagalilea.poketest.db.getDatabase
import com.jotagalilea.poketest.model.Pokemon
import com.jotagalilea.poketest.viewmodel.PokemonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fragmento de la pantalla principal. Contiene la lista de pokemon.
 */
class MainFragment: Fragment(), PokemonRecyclerAdapter.OnItemClickListener {

	private lateinit var viewModel: PokemonViewModel
	private lateinit var recyclerView: RecyclerView
	private lateinit var recyclerAdapter: PokemonRecyclerAdapter
	private lateinit var recyclerLayoutManager: RecyclerView.LayoutManager
	private lateinit var errorImage: ImageView
	private lateinit var errorMsg: TextView
	private lateinit var loader: ProgressBar
	private lateinit var swipeRefreshLayout: SwipeRefreshLayout
	private var dao: PokemonDao? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View {
		return inflater.inflate(R.layout.fragment_main, container, false)
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		viewModel = (activity as MainActivity).getViewModel()
		//viewModel = ViewModelProvider(this).get(PokemonViewModel::class.java)
		dao = context?.let { getDatabase(it).getDao() }
		setupView(view)
		showLoader()
		setupObserver()
		viewModel.loadPokemonMap(true)
		super.onViewCreated(view, savedInstanceState)
	}


	/**
	 * Configura el observador para el modelo de la lista. Permite mostrar un mensaje de error
	 * en caso de que haya un problema en las peticiones.
	 */
	private fun setupObserver(){
		viewModel.getPokemonMap().observe(viewLifecycleOwner,
			Observer<MutableMap<String, Pokemon>> { newMap ->
				hideLoader()
				if (!newMap.isNullOrEmpty()) {
					// Actulización de la lista.
					val newList = newMap.values.toMutableList()
					recyclerAdapter.setItems(newList)
					// Configuración de observadores de datos.
					setupPokemonDataObservers(newList)
					hideErrorMsg()
				}
				else {
					if (newMap == null)
						showErrorMsg()
				}
			}
		)
	}


	/**
	 * Configuración de los observadores para actualizar los pokemon con sus datos.
	 * @param newList Lista de pokemon recibidos en la última petición.
	 */
	private fun setupPokemonDataObservers(newList: MutableList<Pokemon>){
		val it = newList.iterator()
		while (it.hasNext()){
			val item = it.next()
			if (item.data == null)
				item.data = MutableLiveData(Pokemon.Data())
			else {
				if (!item.data!!.value!!.isFromDB)
					MutableLiveData(Pokemon.Data())
			}

			item.data?.observe(viewLifecycleOwner,
				Observer<Pokemon.Data> {
					/*
					 * En el momento en el que se reciben los datos es cuando se hace la inserción
					 * en la BD y se actualiza el recycler con la imagen que contienen.
					 */
					CoroutineScope(Dispatchers.Main).launch {
						dao?.insertOne(item.asDBObject())
						recyclerAdapter.notifyDataSetChanged()
					}
				}
			)
		}
	}


	/**
	 * Configuración de la vista y el listener del scroll.
	 * @param view Vista pasada en el onViewCreated.
	 */
	private fun setupView(view: View){
		recyclerLayoutManager = LinearLayoutManager(activity)
		recyclerAdapter = PokemonRecyclerAdapter(this)
		recyclerView = view.findViewById<RecyclerView>(R.id.pokemon_recycler)!!.apply {
			setHasFixedSize(true)
			layoutManager = recyclerLayoutManager
			adapter = recyclerAdapter
		}
		errorImage = view.findViewById(R.id.main_error_image)
		errorMsg = view.findViewById(R.id.main_error_text)
		loader = view.findViewById(R.id.loader)
		swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
		/*
		 * Con este listener se detecta cuando el usuario llega al final del recycler
		 * para hacer una nueva petición.
		 */
		recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
				super.onScrollStateChanged(recyclerView, newState)
				if (!recyclerView.canScrollVertically(1) && (newState == RecyclerView.SCROLL_STATE_IDLE)){
					try{
						showLoader()
						viewModel.loadPokemonMap(false)
					}
					catch (e: Exception){
						Log.e("ERROR LOADING", e.printStackTrace().toString())
						hideLoader()
						showErrorMsg()
					}
				}
			}
		})

		/*
		 * Con un swipeRefreshLayout puedo recargar la lista deslizando hacia abajo desde
		 * el principio de la misma.
		 */
		swipeRefreshLayout.setOnRefreshListener {
			viewModel.loadPokemonMap(true)
			swipeRefreshLayout.isRefreshing = false
		}
	}


	/**
	 * Muestra el mensaje de error por defecto.
	 */
	private fun showErrorMsg(){
		errorImage.visibility = View.VISIBLE
		errorMsg.visibility = View.VISIBLE
	}

	/**
	 * Oculta el mensaje de error.
	 */
	private fun hideErrorMsg(){
		errorImage.visibility = View.INVISIBLE
		errorMsg.visibility = View.INVISIBLE
	}


	/**
	 * Muestra un progressBar.
	 */
	private fun showLoader(){
		loader.visibility = View.VISIBLE
	}

	/**
	 * Oculta un progressBar.
	 */
	private fun hideLoader(){
		loader.visibility = View.INVISIBLE
	}

	/**
	 * Implementación para el ViewHolder de un elemento de la lista de la navegación a la pantalla
	 * de detalle del pokemon seleccionado.
	 * @param pokemon Pokemon seleccionado por el usuario.
	 */
	override fun onItemClick(pokemon: Pokemon) {
		(activity as MainActivity).navigateToDetail(pokemon)
	}


}