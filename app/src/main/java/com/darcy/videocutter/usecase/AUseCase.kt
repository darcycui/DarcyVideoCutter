package com.darcy.videocutter.usecase

import com.darcy.videocutter.usecase.base.BaseRepositoryUseCase

/**
 * 使用封装的 BaseRepositoryUseCase 示例
 */
class AUseCase(aRepository: ARepository) : BaseRepositoryUseCase<Boolean, String, ARepository>(
    aRepository
) {
    override suspend fun execute(input: Boolean?): String {
        return input?.let { repository.getResult(it) } ?: "error empty input"
    }
}

class ARepository {
    fun getResult(flag: Boolean): String {
        return if (flag) {
            "result ok"
        } else {
            "result failure"
        }
    }
}