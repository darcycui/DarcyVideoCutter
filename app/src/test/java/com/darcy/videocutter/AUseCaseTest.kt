package com.darcy.videocutter

import com.darcy.videocutter.usecase.ARepository
import com.darcy.videocutter.usecase.AUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * 测试 AUseCase
 */
class AUseCaseTest {
    private lateinit var aUseCase: AUseCase
    private lateinit var aRepository: ARepository

    @Before
    fun init() {
        // 创建 ARepository mock 实例
        aRepository = mockk()
        // 创建AUseCase实例 传入 mock 的 ARepository
        aUseCase = AUseCase(aRepository)
    }

    @Test
    fun `should return result success`() {
        // 设置 aRepository.getResult(true) 的返回值
        every { aRepository.getResult(true) } returns "result success"
        every { aRepository.getResult(false) } returns "result failure"

        runBlocking {
            // 断言 AUseCase 的 execute 方法返回结果为 "result success"
            assert(aUseCase.execute(true) == "result success")
            // 验证 getResult 方法被调用一次 且参数是 true
            verify(exactly = 1) { aRepository.getResult(true) }
        }
    }
}