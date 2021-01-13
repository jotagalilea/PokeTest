package com.jotagalilea.poketest.db

import android.content.Context
import androidx.room.*


/**
 * Base de datos con Room.
 */
@Database(entities = [PokemonDBObject::class], version = 1)
abstract class PokemonDB: RoomDatabase() {
    abstract fun getDao(): PokemonDao
}

/**
 * Interfaz para el DAO de la base de datos.
 */
@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon ORDER BY id")
    suspend fun getAllPokemon(): List<PokemonDBObject>

    @Query("SELECT * FROM pokemon ORDER BY id LIMIT :limit OFFSET :offset")
    suspend fun getPokemon(offset: Int, limit: Int): List<PokemonDBObject> //Map<String, Pokemon>

    @Query("SELECT * FROM pokemon WHERE name=:name")
    suspend fun getPokemonWithName(name: String): PokemonDBObject

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(item: PokemonDBObject)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(items: List<PokemonDBObject>)
}

/**
 * Instancia de la base de datos.
 */
private lateinit var INSTANCE: PokemonDB


/**
 * Obtiene la instancia de la base de datos. Si no existe se crea. Implementación básica según la
 * guía de Room.
 * @param context Contexto de la aplicación.
 * @return Instancia de la BD.
 */
fun getDatabase(context: Context): PokemonDB {
    synchronized(PokemonDB::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                PokemonDB::class.java,
                "pokemonDB").build()
        }
    }
    return INSTANCE
}
