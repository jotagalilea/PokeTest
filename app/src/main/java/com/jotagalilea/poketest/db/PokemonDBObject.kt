package com.jotagalilea.poketest.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jotagalilea.poketest.model.Pokemon


/**
 * Tabla de la base de datos y objeto de base de datos de pokemon.
 */
@Entity(tableName = "pokemon")
data class PokemonDBObject(
	@PrimaryKey var name: String,
	var url: String,
	@ColumnInfo(index = true) var id: Int,
	@ColumnInfo(name = "base_experience") var baseExperience: Int,
	var height: Int,
	var weight: Int,
	var types: String,
	@ColumnInfo(name = "image_url") var imageUrl: String?,
	@ColumnInfo(name = "image_file") var imageFile: String?
) {

	/**
	 * Conversor del pokemon objeto de BD en objeto de dominio.
	 * @return Pokemon convertido.
	 */
	fun asDomainModel(): Pokemon{
		return Pokemon(
			name = this.name,
			url = this.url
		).apply {
			data?.setValue(
				Pokemon.Data(
					id,
					baseExperience,
					height,
					weight,
					types,
					imageUrl ?: "",
					imageFile ?: "",
					true
				)
			)
		}
	}

}


/**
 * Conversor de una lista de pokemon de BD en Map de pokemon del dominio, con el nombre como clave.
 * @return Map de pokemon.
 */
fun List<PokemonDBObject>.asDomainModelMap(): Map<String, Pokemon>{
	return map {
		it.name to Pokemon(
			name = it.name,
			url = it.url
		).apply {
			data?.setValue(
				Pokemon.Data(
					it.id,
					it.baseExperience,
					it.height,
					it.weight,
					it.types,
					it.imageUrl ?: "",
					it.imageFile ?: "",
					true
				)
			)
		}
	}.toMap()
}