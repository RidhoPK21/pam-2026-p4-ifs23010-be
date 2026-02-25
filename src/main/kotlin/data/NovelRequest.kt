package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Novel

@Serializable
data class NovelRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var genre: String = "",
    var karakterUtama: String = "",
    var penulis: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "genre" to genre,
            "karakterUtama" to karakterUtama,
            "penulis" to penulis,
            "pathGambar" to pathGambar
        )
    }

    fun toEntity(): Novel {
        return Novel(
            nama = nama,
            deskripsi = deskripsi,
            genre = genre,
            karakterUtama = karakterUtama,
            penulis = penulis,
            pathGambar =  pathGambar,
        )
    }

}