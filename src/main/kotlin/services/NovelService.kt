package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.NovelRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.INovelRepository
import java.io.File
import java.util.*

class NovelService(private val novelRepository: INovelRepository) {
    // Mengambil semua data tumbuhan
    suspend fun getAllNovels(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""

        val novels = novelRepository.getNovels(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar novel",
            mapOf(Pair("novels", novels))
        )
        call.respond(response)
    }

    // Mengambil data tumbuhan berdasarkan id
    suspend fun getNovelById(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID novel tidak boleh kosong!")

        val novel = novelRepository.getNovelById(id) ?: throw AppException(404, "Data novel tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data novel",
            mapOf(Pair("novel", novel))
        )
        call.respond(response)
    }

    // Ambil data request
    private suspend fun getNovelRequest(call: ApplicationCall): NovelRequest {
        // Buat object penampung
        val novelReq = NovelRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                // Ambil request berupa teks
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> novelReq.nama = part.value.trim()
                        "deskripsi" -> novelReq.deskripsi = part.value
                        "genre" -> novelReq.genre = part.value
                        "karakterUtama" -> novelReq.karakterUtama = part.value
                        "penulis" -> novelReq.penulis = part.value
                    }
                }

                // Upload file
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/novels/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs() // pastikan folder ada

                    part.provider().copyAndClose(file.writeChannel())
                    novelReq.pathGambar = filePath
                }

                else -> {}
            }

            part.dispose()
        }

        return novelReq
    }

    // Validasi request data dari pengguna
    private fun validateNovelRequest(novelReq: NovelRequest){
        val validatorHelper = ValidatorHelper(novelReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("genre", "genre tidak boleh kosong")
        validatorHelper.required("karakterUtama", "Karakter Utama tidak boleh kosong")
        validatorHelper.required("penulis", "Penulis tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        validatorHelper.validate()

        val file = File(novelReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar novel gagal diupload!")
        }

    }

    // Menambahkan data tumbuhan
    suspend fun createNovel(call: ApplicationCall) {
        // Ambil data request
        val novelReq = getNovelRequest(call)

        // Validasi request
        validateNovelRequest(novelReq)

        // periksa novel dengan nama yang sama
        val existNovel = novelRepository.getNovelByName(novelReq.nama)
        if(existNovel != null){
            val tmpFile = File(novelReq.pathGambar)
            if(tmpFile.exists()){
                tmpFile.delete()
            }
            throw AppException(409, "Novel dengan nama ini sudah terdaftar!")
        }

        val novelId = novelRepository.addNovel(
            novelReq.toEntity()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data novel",
            mapOf(Pair("novelId", novelId))
        )
        call.respond(response)
    }

    // Mengubah data tumbuhan
    suspend fun updateNovel(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID novel tidak boleh kosong!")

        val oldNovel = novelRepository.getNovelById(id) ?: throw AppException(404, "Data novel tidak tersedia!")

        // Ambil data request
        val novelReq = getNovelRequest(call)

        if(novelReq.pathGambar.isEmpty()){
            novelReq.pathGambar = oldNovel.pathGambar
        }

        // Validasi request
        validateNovelRequest(novelReq)

        // periksa novel dengan nama yang sama jika nama diubah
        if(novelReq.nama != oldNovel.nama){
            val existNovel = novelRepository.getNovelByName(novelReq.nama)
            if(existNovel != null){
                val tmpFile = File(novelReq.pathGambar)
                if(tmpFile.exists()){
                    tmpFile.delete()
                }
                throw AppException(409, "Novel dengan nama ini sudah terdaftar!")
            }
        }

        // Hapus gambar lama jika mengupload file baru
        if(novelReq.pathGambar != oldNovel.pathGambar){
            val oldFile = File(oldNovel.pathGambar)
            if(oldFile.exists()){
                oldFile.delete()
            }
        }

        val isUpdated = novelRepository.updateNovel(
            id, novelReq.toEntity()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data novel!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data novel",
            null
        )
        call.respond(response)
    }

    // Menghapus data tumbuhan
    suspend fun deleteNovel(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID novel tidak boleh kosong!")

        val oldNovel = novelRepository.getNovelById(id) ?: throw AppException(404, "Data novel tidak tersedia!")

        val oldFile = File(oldNovel.pathGambar)

        val isDeleted = novelRepository.removeNovel(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data novel!")
        }

        // Hapus data gambar jika data tumbuhan sudah dihapus
        if (oldFile.exists()) {
            oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data novel",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar tumbuhan
    suspend fun getNovelImage(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val novel = novelRepository.getNovelById(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(novel.pathGambar)

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}