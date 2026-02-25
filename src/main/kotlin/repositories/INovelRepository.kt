package org.delcom.repositories

import org.delcom.entities.Novel

interface  INovelRepository {
    suspend fun getNovels(search: String): List<Novel>
    suspend fun getNovelById(id: String): Novel?
    suspend fun getNovelByName(name: String): Novel?
    suspend fun addNovel(novel: Novel) : String
    suspend fun updateNovel(id: String, newNovel: Novel): Boolean
    suspend fun removeNovel(id: String): Boolean
}