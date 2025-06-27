package com.darcy.videocutter.usecase.base

abstract class BaseRepositoryUseCase<In, Out, Repository>(
    override var repository: Repository
) : IRepositoryUseCase<In, Out, Repository> {
    suspend operator fun invoke(params: In? = null): Out {
        return execute(params)
    }
}