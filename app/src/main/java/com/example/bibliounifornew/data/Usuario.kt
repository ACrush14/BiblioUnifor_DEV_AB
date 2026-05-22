package com.example.bibliounifornew.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_usuarios")
data class Usuario(
    @PrimaryKey
    val uid: String = "", //id universao ai do firebase
    val nome: String = "",
    val usuario: String = "", //username
    val email: String = "",
    val fotoUrl: String = "", // caminho da img perfil
    val tipoPerfil: String = "", // estudante ou adm
    val statusCadastro: String = "" //pendente ou aprov
)