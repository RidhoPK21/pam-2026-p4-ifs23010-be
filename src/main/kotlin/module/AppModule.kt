package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.services.PlantService
import org.delcom.repositories.INovelRepository
import org.delcom.repositories.NovelRepository
import org.delcom.services.NovelService
import org.delcom.services.ProfileService
import org.koin.dsl.module


val appModule = module {
    // Plant Repository
    single<IPlantRepository> {
        PlantRepository()
    }

    // Plant Service
    single {
        PlantService(get())
    }

    // Novel Repository
    single<INovelRepository> {
        NovelRepository()
    }

    // Novel Service
    single {
        NovelService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}