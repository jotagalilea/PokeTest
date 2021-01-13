package com.jotagalilea.poketest.model

import androidx.lifecycle.MutableLiveData
import com.google.gson.annotations.SerializedName
import com.jotagalilea.poketest.apiclient.PokemonDataResponse
import com.jotagalilea.poketest.db.PokemonDBObject
import com.jotagalilea.poketest.typesToString
import java.io.Serializable

/**
 * Clase modelo de un pokemon.
 */
data class Pokemon(
	@SerializedName("name") var name: String,
	@SerializedName("url") var url: String
): Serializable {

	var data: MutableLiveData<Data>? = MutableLiveData(Data())

	/**
	 * Modelo de datos de un pokemon.
	 */
	class Data() {
		var id: Int = 0
		var baseExperience: Int = 0
		var height: Int = 0
		var weight: Int = 0
		var types: List<PokemonDataResponse.TypeContainer>? = null
		var imageUrl: String? = null
		var imageFile: String? = null
		var isFromDB: Boolean = false

		/**
		 * Constructor secundario. Sirve tanto para crear los datos contenidos en la BD como los recibidos en respuesta al servicio.
		 */
		constructor(id: Int, exp: Int, hei: Int, wei: Int, typ: String, iUrl: String, iFile: String, fromDB: Boolean) : this() {
			this.id = id
			baseExperience = exp
			height = hei
			weight = wei
			imageUrl = iUrl
			imageFile = iFile
			val listTypesStr: List<String> = typ.split(", ")
			val listTypes: MutableList<PokemonDataResponse.TypeContainer> = mutableListOf()
			val it = listTypesStr.iterator()
			while (it.hasNext()){
				val t = PokemonDataResponse.TypeContainer(PokemonDataResponse.TypeContainer.Type(it.next()))
				listTypes.add(t)
			}
			types = listTypes
			isFromDB = fromDB
		}
	}


	/**
	 * Conversor del objeto pokemon del dominio en objeto de BD.
	 * @return Pokemon para la BD.
	 */
	fun asDBObject(): PokemonDBObject{
		return PokemonDBObject(
			name = name,
			url = url,
			id = data?.value?.id ?: 0,
			baseExperience = data?.value?.id ?: 0,
			height = data?.value?.height ?: 0,
			weight = data?.value?.weight ?: 0,
			types = data?.value?.types.typesToString(),
			imageUrl = data?.value?.imageUrl ?: "",
			imageFile = data?.value?.imageFile ?: ""
		)
	}

}