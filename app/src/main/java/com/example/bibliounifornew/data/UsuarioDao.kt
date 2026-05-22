package com.example.bibliounifornew.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    // insere ou atualizar os users logados ai se ele ja existir substitui com os dados novos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirOuAtualizarUsuario(usuario: Usuario)

    // Busca os dados do usuário logado pra montar a dashboard do 08
    @Query("SELECT * FROM tabela_usuarios WHERE uid = :uid LIMIT 1")
    suspend fun obterUsuarioPorUid(uid: String): Usuario?

    // Deleta os dados locais quando o usuario deslogar do app
    @Query("DELETE FROM tabela_usuarios")
    suspend fun deslogarUsuarioLocal()
}