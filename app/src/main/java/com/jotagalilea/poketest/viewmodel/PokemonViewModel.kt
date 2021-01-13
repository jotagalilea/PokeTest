package com.jotagalilea.poketest.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.jotagalilea.poketest.apiclient.PokemonDataResponse
import com.jotagalilea.poketest.apiclient.PokemonEndpoints
import com.jotagalilea.poketest.apiclient.PokemonResponse
import com.jotagalilea.poketest.db.PokemonDao
import com.jotagalilea.poketest.db.asDomainModelMap
import com.jotagalilea.poketest.db.getDatabase
import com.jotagalilea.poketest.model.Pokemon
import com.jotagalilea.poketest.typesToString
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Viewmodel que tiene las referencias a objetos del modelo, acceso a base de datos y api web.
 */
class PokemonViewModel(application: Application) : AndroidViewModel(application) {
	private val TAG = "Obteniendo Pokemon..."
	private val TAG_ERROR = "Error obteniendo datos"
	private var requestOffset: Int = 0
	private val requestLimit: Int = 10
	private var pokemonResponse: PokemonResponse? = null
	private var pokemonDataResponse: PokemonDataResponse? = null
	private var pokemonMap: MutableLiveData<MutableMap<String, Pokemon>>
	private var dao: PokemonDao
	private val retrofit: Retrofit
	private val api: PokemonEndpoints


	init {
		dao = getDatabase(application.applicationContext).getDao()
		retrofit = Retrofit.Builder()
			.baseUrl(PokemonEndpoints.BASE_URL)
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		api = retrofit.create(PokemonEndpoints::class.java)
		pokemonMap = MutableLiveData(mutableMapOf())
	}


	/**
	 * Carga de ítems en el modelo de BD o de servicio web, según se necesite.
	 * @param reload Indica si hay que recargar el map.
	 */
	fun loadPokemonMap(reload: Boolean){
		viewModelScope.launch {
			if (reload) {
				requestOffset = 0
				pokemonMap.value?.clear()
			}
			val dbItems: Map<String, Pokemon> = dao.getPokemon(requestOffset, requestLimit).asDomainModelMap()
			if (!dbItems.isNullOrEmpty()) {
				requestOffset += requestLimit
				pokemonMap.addNewItems(dbItems)
			}
			else {
				requestPokemonList()
			}
		}
	}

	/**
	 * Extensión de MutableLiveData<MutableMap<String, Pokemon>> para añadir elementos al pokemonMap.
	 * Versión para cuando se recibe un MutableList<Pokemon>.
	 * @param newList Lista con nuevos elementos.
	 */
	private fun MutableLiveData<MutableMap<String, Pokemon>>.addNewItems(newList: MutableList<Pokemon>){
		val map = this.value
		val newItems = newList.map { it.name to it }.toMap()
		map?.putAll(newItems)
		this.postValue(map)
	}
	/**
	 * Extensión de MutableLiveData<MutableMap<String, Pokemon>> para añadir elementos al pokemonMap.
	 * Versión para cuando se recibe un Map.
	 * @param newMap Map con nuevos elementos.
	 */
	private fun MutableLiveData<MutableMap<String, Pokemon>>.addNewItems(newMap: Map<String, Pokemon>){
		val old = this.value
		old?.putAll(newMap)
		this.postValue(old)
	}

	/**
	 * Devuelve el map de pokemon.
	 * @return Map de pokemon.
	 */
	fun getPokemonMap(): MutableLiveData<MutableMap<String, Pokemon>> {
		return pokemonMap
	}


	/**
	 * Realiza la petición al servicio web para obtener una serie de pokemon.
	 * Además lanza las peticiones necesarias para obtener los datos de los pokemon.
	 */
	private fun requestPokemonList(){
		val call: Call<PokemonResponse> = api.getPokemonList(requestOffset, requestLimit)
		call.enqueue(object : Callback<PokemonResponse> {
			override fun onResponse(
				call: Call<PokemonResponse>,
				response: Response<PokemonResponse>
			) {
				if (response.isSuccessful) {
					pokemonResponse = response.body()
					pokemonResponse?.responseList?.let { newList ->
						pokemonMap.addNewItems(newList)
						val map = pokemonMap.value
						val newItems = newList.map { it.name to it }.toMap()
						map?.putAll(newItems)
						pokemonMap.setValue(map)
						requestNewPokemonData(newItems)
					}
					requestOffset += requestLimit
				} else {
					pokemonMap.value?.clear()
					requestOffset = 0
					Log.e(TAG_ERROR, response.message())
				}
			}

			override fun onFailure(call: Call<PokemonResponse>, t: Throwable) {
				pokemonMap.value?.clear()
				requestOffset = 0
				Log.e(TAG_ERROR, call.toString())
			}

		})
	}


	/**
	 * Lanza las peticiones para tomar los datos de la lista de pokemon guardada en la anterior petición.
	 * @param newItems Map de nuevos elementos aún sin los datos extra.
	 */
	fun requestNewPokemonData(newItems: Map<String, Pokemon>){
		val it = newItems.iterator()
		while (it.hasNext()){
			val item = it.next()
			val call: Call<PokemonDataResponse> = api.getPokemonData(item.key)
			call.enqueue(object : Callback<PokemonDataResponse> {
				override fun onResponse(
					call: Call<PokemonDataResponse>,
					response: Response<PokemonDataResponse>
				) {
					if (response.isSuccessful) {
						pokemonDataResponse = response.body()
						setItemData(item.key)
					} else {
						Log.e(TAG_ERROR, call.toString())
					}
				}

				override fun onFailure(call: Call<PokemonDataResponse>, t: Throwable) {
					Log.e(TAG_ERROR, call.toString())
					throw t
				}
			})
		}
	}

	/**
	 * @param key Clave del pokemon que se está actualizando.
	 */
	private fun setItemData(key: String){
		val pokemon: Pokemon? = pokemonMap.value?.get(key)
		pokemon?.let {
			val data = Pokemon.Data(
				id = pokemonDataResponse?.id ?: 0,
				exp = pokemonDataResponse?.baseExperience ?: 0,
				hei = pokemonDataResponse?.height ?: 0,
				wei = pokemonDataResponse?.weight ?: 0,
				typ = pokemonDataResponse?.typesList.typesToString(),
				iUrl = pokemonDataResponse?.spritesContainer?.spriteOther?.spriteOfficial?.spriteURL ?: "",
				iFile = "",
				fromDB = false
			)
			it.data?.setValue(data)
		}
	}
}
