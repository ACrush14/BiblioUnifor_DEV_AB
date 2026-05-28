package com.example.bibliounifornew.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Histórico de versões do schema Room:
 *
 *  v1 → v2  Adição de campos iniciais (EntidadeLivro, Usuario)
 *  v2 → v3  Ajustes de tipos/colunas intermediários
 *  v3 → v4  Adição de suporte à TelaLivroActivity (offline-first):
 *           campos coverUrl, isAvailable, stockQuantity em EntidadeLivro
 *  v4 → v5  Suporte a links de mídia digital (RF12):
 *           campos linkPdf e linkAudiobook em EntidadeLivro
 *
 * Estratégia de migração (desenvolvimento local):
 *   fallbackToDestructiveMigration — descarta o banco local e recria as
 *   tabelas a partir do schema atual. Seguro em dev porque todos os dados
 *   persistentes ficam no Firestore; o Room atua apenas como cache local.
 *   Para produção, substituir por uma classe Migration explícita.
 */
@Database(entities = [EntidadeLivro::class, Usuario::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun livroDao(): LivroDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bibliounifor_database"
                )
                    // Descarta e recria o banco local ao detectar divergência de schema.
                    // Correto para desenvolvimento — todos os dados canônicos estão no Firestore.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}