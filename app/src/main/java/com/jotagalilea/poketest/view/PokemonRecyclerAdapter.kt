package com.jotagalilea.poketest.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.jotagalilea.poketest.R
import com.jotagalilea.poketest.model.Pokemon
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception


/**
 * Adaptador para un recycler de pokemon.
 */
class PokemonRecyclerAdapter(private val onItemClickListener: OnItemClickListener)
	: RecyclerView.Adapter<PokemonRecyclerAdapter.PokemonRowViewHolder>() {

	private var pokemonList: MutableLiveData<MutableList<Pokemon>> = MutableLiveData(mutableListOf())
	private val TAG_PICASSO_ERROR = "PICASSO ERROR"


	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonRowViewHolder {
		val container = LayoutInflater.from(parent.context)
			.inflate(R.layout.item_pokemon, parent, false) as ConstraintLayout
		return PokemonRowViewHolder(container, onItemClickListener)

	}


	override fun onBindViewHolder(holder: PokemonRowViewHolder, position: Int) {
		// Construcción de la ruta de la imagen
		val item = pokemonList.value?.get(position)
		holder.name.text = item?.name

		// Si existe url para la imagen entonces se carga con Picasso. Si falla se muestra un icono de error.
		if (item?.data != null){
			val path = item.data?.value!!.imageUrl
			Picasso.get().isLoggingEnabled = true
			Picasso.get()
				.load(path)
				.error(R.drawable.ic_error)
				.into(holder.image, object: Callback {
					override fun onSuccess() {
						holder.hideLoader()
					}
					override fun onError(e: Exception?) {
						holder.hideLoader()
						Log.e(TAG_PICASSO_ERROR, e?.stackTrace.toString())
					}
				})
		}
	}


	/**
	 * Actualiza el recycler con nuevos elementos.
	 * @param newItems Nuevos pokemon para agregar.
	 */
	fun setItems(newItems: MutableList<Pokemon>){
		pokemonList.postValue(newItems)
		notifyDataSetChanged()
	}


	/**
	 * Limpia el recycler.
	 */
	fun clearItems(){
		pokemonList.value?.clear()
		notifyDataSetChanged()
	}


	override fun getItemCount(): Int {
		return pokemonList.value?.size ?: 0
	}


	/**
	 * Interfaz para hacer más fácil acoplar distintos onClickListener al viewHolder del recycler.
	 */
	interface OnItemClickListener {
		fun onItemClick(pokemon: Pokemon)
	}


	//------------------------- ViewHolder -----------------------------//
	/**
	 * ViewHolder para mostrar el nombre y la imagen de cada pokemon.
	 */
	inner class PokemonRowViewHolder(
		itemView: View,
		private var onItemClickListener: OnItemClickListener
	) : RecyclerView.ViewHolder(itemView), View.OnClickListener  {

		var image: ImageView = itemView.findViewById(R.id.poke_img_list)
		var name: TextView = itemView.findViewById(R.id.poke_name_list)
		private var loader: ProgressBar = itemView.findViewById(R.id.img_loader)

		/**
		 * A la vista que contiene el view holder se le asigna el evento de click del view holder.
		 * Es útil si este viewHolder se usa en más de una vista, ya que permite que cada una tenga
		 * distinta implementación del onClick, la cual se debería hacer en la misma vista. En el
		 * caso particular de esta aplicación, como no se va a tener este view holder fuera de
		 * un recycler se puede tener como clase interna del adaptador, aunque está preparado para
		 * otro caso.
		 */
		init {
			itemView.setOnClickListener(this)
		}

		/**
		 * Llama al listener de la vista en la que está implementado.
		 */
		override fun onClick(v: View?) {
			val selectedItem: Pokemon = pokemonList.value!![adapterPosition]
			onItemClickListener.onItemClick(selectedItem)
		}

		/**
		 * Oculta un progressBar.
		 */
		fun hideLoader(){
			loader.visibility = View.GONE
		}

		/**
		 * Muestra un progressBar.
		 */
		fun showLoader(){
			loader.visibility = View.VISIBLE
		}
	}
}