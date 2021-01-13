package com.jotagalilea.poketest.apiclient

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Clase receptora de datos de pokemon.
 */
@Serializable
data class PokemonDataResponse(
	@SerializedName("id") val id: Int?,
	@SerializedName("is_default") val isDefault: Boolean?,
	@SerializedName("base_experience") val baseExperience: Int?,
	@SerializedName("height") val height: Int?,
	@SerializedName("weight") val weight: Int?,
	@SerializedName("types") val typesList: List<TypeContainer>?,
	@SerializedName("sprites") val spritesContainer: SpritesContainer?
) {
	/**
	 * Contenedor de tipos de un pokemon. Puede tener más de uno.
	 */
	@Serializable
	data class TypeContainer(
		@SerializedName("type") val type: Type?
	) {
		companion object{
			const val EMPTY_TYPE: String = "No type"
		}

		/**
		 * Tipo de un pokemon.
		 */
		@Serializable
		data class Type(
			@SerializedName("name") val name: String?
		)
	}

	/**
	 * Contenedor de sprites (imagen) de un pokemon. Selector para "other" en el json.
	 */
	@Serializable
	data class SpritesContainer(
		@SerializedName("other") val spriteOther: SpriteOfficial?
	) {
		/**
		 * Selector para "official-artwork" en el json.
		 */
		@Serializable
		data class SpriteOfficial(
			@SerializedName("official-artwork") val spriteOfficial: SpriteFrontDefault?
		) {
			/**
			 * URL del sprite. Selector para "front-default" en el json.
			 */
			@Serializable
			data class SpriteFrontDefault(
				@SerializedName("front_default") val spriteURL: String?
			)
		}
	}
}