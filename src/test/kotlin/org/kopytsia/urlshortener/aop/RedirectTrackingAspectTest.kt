package org.kopytsia.urlshortener.aop

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.kopytsia.urlshortener.constants.TestConstants.Codes.VALID_CUSTOM
import org.kopytsia.urlshortener.service.ClickEventService

@ExtendWith(MockKExtension::class)
class RedirectTrackingAspectTest {

    @MockK
    lateinit var clickEventService: ClickEventService
    @MockK
    lateinit var pjp: ProceedingJoinPoint
    @MockK
    lateinit var httpServletRequest: HttpServletRequest
    @InjectMockKs
    lateinit var redirectTrackingAspect: RedirectTrackingAspect

    @BeforeEach
    fun setup() = clearMocks(clickEventService, pjp, httpServletRequest)

    @Test
    fun `test calls click service and proceeds`() {
        every { clickEventService.logAsync(VALID_CUSTOM, httpServletRequest) } just Runs
        every { pjp.proceed() } returns "200"

        assertEquals("200", redirectTrackingAspect.around(pjp, VALID_CUSTOM, httpServletRequest))
        verifyOrder {
            clickEventService.logAsync(VALID_CUSTOM, httpServletRequest)
            pjp.proceed()
        }
        confirmVerified(clickEventService, pjp)
    }

    @Test
    fun `test propagates exceptions from join point`() {
        every { clickEventService.logAsync(VALID_CUSTOM, httpServletRequest) } just Runs
        every { pjp.proceed() } throws IllegalStateException()

        assertThrows(IllegalStateException::class.java) {
            redirectTrackingAspect.around(pjp, VALID_CUSTOM, httpServletRequest)
        }

        verify { clickEventService.logAsync(VALID_CUSTOM, httpServletRequest) }
        verify { pjp.proceed() }
    }
}

