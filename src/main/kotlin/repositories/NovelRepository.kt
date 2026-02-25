package org.delcom.repositories

import org.delcom.dao.NovelDAO
import org.delcom.entities.Novel
import org.delcom.helpers.daoToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.NovelTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class NovelRepository : INovelRepository {
    override suspend fun getNovels(search: String): List<Novel> = suspendTransaction {
        if (search.isBlank()) {
            NovelDAO.all()
                .orderBy(NovelTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModel)
        } else {
            val keyword = "%${search.lowercase()}%"

            NovelDAO
                .find {
                    NovelTable.nama.lowerCase() like keyword
                }
                .orderBy(NovelTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModel)
        }
    }

    override suspend fun getNovelById(id: String): Novel? = suspendTransaction {
        NovelDAO
            .find { (NovelTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun getNovelByName(name: String): Novel? = suspendTransaction {
        NovelDAO
            .find { (NovelTable.nama eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addNovel(novel: Novel): String = suspendTransaction {
        val novelDAO = NovelDAO.new {
            nama = novel.nama
            pathGambar = novel.pathGambar
            deskripsi = novel.deskripsi
            genre = novel.genre
            karakterUtama = novel.karakterUtama
            penulis = novel.penulis
            createdAt = novel.createdAt
            updatedAt = novel.updatedAt
        }

        novelDAO.id.value.toString()
    }

    override suspend fun updateNovel(id: String, newNovel: Novel): Boolean = suspendTransaction {
        val novelDAO = NovelDAO
            .find { NovelTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (novelDAO != null) {
            novelDAO.nama = newNovel.nama
            novelDAO.pathGambar = newNovel.pathGambar
            novelDAO.deskripsi = newNovel.deskripsi
            novelDAO.genre = newNovel.genre
            novelDAO.karakterUtama = newNovel.karakterUtama
            novelDAO.penulis = newNovel.penulis
            novelDAO.updatedAt = newNovel.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removeNovel(id: String): Boolean = suspendTransaction {
        val rowsDeleted = NovelTable.deleteWhere {
            NovelTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }

}