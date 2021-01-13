package com.jotagalilea.poketest.apiclient

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


/**
 * Endpoints de los servicios PokeAPI utilizados en la aplicación.
 */
interface PokemonEndpoints {

	//TODO: Pensar cómo dejo este archivo, si lo dejo como object, top level o cómo.

	/**
	 * Variables utilizadas en las requests.
	 */
	companion object{
		const val BASE_URL = "https://pokeapi.co/api/v2/"
		const val POKEMON_LIST_URL = "https://pokeapi.co/api/v2/pokemon/"
	}


	/**
	 * Petición de un conjunto de pokemon.
	 * @param offset Desplazamiento.
	 * @param limit Límite de elementos.
	 */
	@GET("pokemon/")
	fun getPokemonList(
		@Query("offset") offset: Int,
		@Query("limit") limit: Int
	): Call<PokemonResponse>


	/**
	 * Petición de los datos de un pokemon.
	 * @param name nombre del pokemon del que se obtendrán los datos.
	 */
	@GET("pokemon/{name}")
	fun getPokemonData(
		@Path("name") name: String
	): Call<PokemonDataResponse>

}