package org.delcom.dao

import org.delcom.tables.NovelTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class NovelDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, NovelDAO>(NovelTable)

    var nama by NovelTable.nama
    var pathGambar by NovelTable.pathGambar
    var deskripsi by NovelTable.deskripsi
    var genre by NovelTable.genre
    var karakterUtama by NovelTable.karakterUtama
    var penulis by NovelTable.penulis
    var createdAt by NovelTable.createdAt
    var updatedAt by NovelTable.updatedAt
}