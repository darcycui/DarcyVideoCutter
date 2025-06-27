package com.darcy.videocutter.usecase.base

interface IRepositoryUseCase<In, Out, Repository> : IUseCase<In, Out> {
    var repository: Repository
}