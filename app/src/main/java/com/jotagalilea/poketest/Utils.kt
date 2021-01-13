package com.jotagalilea.poketest

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.jotagalilea.poketest.apiclient.PokemonDataResponse
import com.jotagalilea.poketest.db.getDatabase
import com.jotagalilea.poketest.model.Pokemon
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


//TODO: Mover a PokemonDataResponse.
fun List<PokemonDataResponse.TypeContainer>?.typesToString() : String {
	val types = this
	return StringBuilder().apply {
		if (types == null)
			append(PokemonDataResponse.TypeContainer.EMPTY_TYPE)
		else {
			append(types[0].type?.name)
			if (types.size > 1) {
				append(", ")
				append(types[1].type?.name)
			}
		}
	}.toString()
}


// Para logging con Picasso:
private const val TAG_PICASSO_ERROR = "PICASSO ERROR"
private const val TAG_PICASSO_SUCCESS = "PICASSO SUCCESS"


/**
 * Guarda la imagen obtenida de un pokemon en un fichero con formato png.
 * @param pokemon Pokemon del cual se obtuvo la imagen. Se actualiza el parámetro imageFile.
 * @param drawable Imagen a guardar.
 * @param fileName Nombre del fichero.
 * @param context Contexto de la aplicación.
 */
private fun saveImageToFile(pokemon: Pokemon, drawable: Drawable, fileName: String, context: Context?){
	val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
	val storage: File? = context?.externalCacheDir
	val dir = File(storage?.absolutePath + "/PokemonImgs")
	dir.mkdirs()
	val outFile = File(dir, fileName)
	val outStream = FileOutputStream(outFile)
	bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
	outStream.flush()
	outStream.close()
	pokemon.data?.value?.imageFile = outFile.absolutePath
}


/**
 * Carga de la imagen de un pokemon previamente guardada en un fichero. Se hace uso de la API Picasso.
 * @param path Ruta del fichero.
 * @param imageView ImageView en el que se cargará la imagen.
 */
fun loadImageFromFile(path: String, imageView: ImageView){
	Picasso.get().isLoggingEnabled = true
	Picasso.get()
		.load(File(path))
		.error(R.drawable.ic_error)
		.into(imageView, object : Callback {
			override fun onSuccess() {
				Log.d(TAG_PICASSO_SUCCESS, "Success getting image")
			}

			override fun onError(e: Exception?) {
				imageView.setImageResource(R.drawable.ic_error)
				Log.e(TAG_PICASSO_ERROR, e?.message.toString())
			}
		})
}


/**
 * Carga de la imagen de un pokemon a partir de una url. Se hace uso de la API Picasso.
 * @param pokemon Pokemon al cual pertenece al imagen.
 * @param path Ruta del fichero.
 * @param imageView ImageView en el que se cargará la imagen.
 * @param context Contexto de la aplicación.
 */
fun loadImageFromUrl(pokemon: Pokemon, path: String, imageView: ImageView, context: Context?){
	Picasso.get().isLoggingEnabled = true
	Picasso.get()
		.load(path)
		.error(R.drawable.ic_error)
		.into(imageView, object : Callback {
			override fun onSuccess() {
				Log.d(TAG_PICASSO_SUCCESS, "Success getting image")
				val fileName = pokemon.name + ".png"
				saveImageToFile(pokemon, imageView.drawable, fileName, context)
				updatePokemonInDB(pokemon, context)
			}

			override fun onError(e: Exception?) {
				imageView.setImageResource(R.drawable.ic_error)
				Log.e(TAG_PICASSO_ERROR, e?.message.toString())
			}
		})
}


/**
 * Actualización de la info guardada de un pokemon en la BD.
 * @param pokemon Pokemon con datos actualizados.
 * @param context Contexto de aplicación.
 */
private fun updatePokemonInDB(pokemon: Pokemon, context: Context?) = CoroutineScope(Dispatchers.Main).launch {
	val dao = context?.let { getDatabase(it).getDao() }
	dao?.insertOne(pokemon.asDBObject())
}
