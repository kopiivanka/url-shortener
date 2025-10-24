package org.kopytsia.urlshortener.aop

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import org.kopytsia.urlshortener.service.ClickEventService

@Aspect
@Component
class RedirectTrackingAspect(
    private val clickEventService: ClickEventService
) {
    @Around("@annotation(TrackRedirect) && args(code, request, ..)")
    fun around(
        pjp: ProceedingJoinPoint,
        code: String,
        request: HttpServletRequest
    ): Any? {
        clickEventService.logAsync(code, request)
        return pjp.proceed()
    }
}