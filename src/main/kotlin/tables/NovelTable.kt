package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object NovelTable : UUIDTable("novels") {
    val nama = varchar("nama", 100)
    val pathGambar = varchar("path_gambar", 255)
    val deskripsi = text("deskripsi")
    val genre = text("genre")
    val karakterUtama = text("karakter_utama")
    val penulis = text("penulis")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}