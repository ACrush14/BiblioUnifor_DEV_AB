package com.example.bibliounifornew.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {

    // REPLACE garante que se o usuário já existir localmente (ex: fez login em outro aparelho e baixou os dados), ele atualiza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirUsuario(usuario: Usuario)

    @Query("SELECT * FROM tabela_usuarios WHERE uid = :uid")
    suspend fun buscarUsuarioPorUid(uid: String): Usuario?

    @Query("SELECT * FROM tabela_usuarios")
    suspend fun buscarTodosUsuarios(): List<Usuario>

    @Update
    suspend fun atualizarUsuario(usuario: Usuario)
}