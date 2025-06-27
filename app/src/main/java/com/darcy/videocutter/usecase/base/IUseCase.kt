package com.darcy.videocutter.usecase.base

interface IUseCase<In, Out> {
    suspend fun execute(input: In?): Out
}

