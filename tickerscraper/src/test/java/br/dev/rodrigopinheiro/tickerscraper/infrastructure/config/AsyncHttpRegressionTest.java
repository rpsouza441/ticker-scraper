package br.dev.rodrigopinheiro.tickerscraper.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.web.context.request.ServletWebRequest;
import br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.exception.GlobalExceptionHandler;
import static org.junit.jupiter.api.Assertions.*;

class AsyncHttpRegressionTest {
    @Test void rawEndpointTimeoutHasErrorHttpAndBodyWithSameCorrelationId() throws Exception {
        var useCase=org.mockito.Mockito.mock(br.dev.rodrigopinheiro.tickerscraper.application.port.input.AcaoUseCasePort.class);
        var mapper=org.mockito.Mockito.mock(br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.mapper.AcaoApiMapper.class);
        org.mockito.Mockito.when(useCase.getRawTickerData("SAPR11")).thenReturn(reactor.core.publisher.Mono.error(
                new org.springframework.web.context.request.async.AsyncRequestTimeoutException()));
        var mvc=org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new br.dev.rodrigopinheiro.tickerscraper.adapter.input.web.AcaoController(useCase,mapper))
                .setControllerAdvice(new GlobalExceptionHandler()).addInterceptors(new CorrelationIdInterceptor()).build();
        var initial=mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/acao/get-SAPR11/raw"))
                .andReturn();
        String id=initial.getResponse().getHeader("X-Correlation-ID");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(initial))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isGatewayTimeout())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code").value("SCRAPING_TIMEOUT"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.correlationId").value(id));
    }
    @Test void asyncRedispatchKeepsGeneratedCorrelationIdAndTimeoutIs504() {
        var interceptor=new CorrelationIdInterceptor();
        var request=new MockHttpServletRequest("GET","/acao/get-SAPR11");
        var response=new MockHttpServletResponse();
        interceptor.preHandle(request,response,new Object());
        String id=response.getHeader("X-Correlation-ID");assertNotNull(id);
        interceptor.afterConcurrentHandlingStarted(request,response,new Object());
        assertNull(CorrelationIdInterceptor.getCurrentCorrelationId());
        interceptor.preHandle(request,response,new Object());
        var error=new GlobalExceptionHandler().handleFrameworkTimeout(
                new org.springframework.web.context.request.async.AsyncRequestTimeoutException(),new ServletWebRequest(request));
        assertEquals(504,error.getStatusCode().value());
        assertEquals(id,response.getHeader("X-Correlation-ID"));
        assertEquals(id,error.getBody().correlationId());
        interceptor.afterCompletion(request,response,new Object(),null);
    }
    @Test void invalidTickerRejectedBeforeScrapingWithTraceId() {
        var request=new MockHttpServletRequest("GET","/acao/get-invalid");
        request.setAttribute(org.springframework.web.servlet.HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,java.util.Map.of("ticker","invalid"));
        var response=new MockHttpServletResponse();
        assertThrows(IllegalArgumentException.class,()->new CorrelationIdInterceptor().preHandle(request,response,new Object()));
        assertNotNull(response.getHeader("X-Correlation-ID"));CorrelationIdInterceptor.clearCorrelationId();
    }
}
