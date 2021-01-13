package com.jotagalilea.poketest.apiclient

import com.google.gson.annotations.SerializedName
import com.jotagalilea.poketest.model.Pokemon

/**
 * Clase para capturar una lista de pokemon json.
 */
data class PokemonResponse(
	@SerializedName("results") var responseList: MutableList<Pokemon>,
	@SerializedName("count") var count: Int
)