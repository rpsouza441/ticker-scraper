package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base;

import org.junit.jupiter.api.Test;
import org.jsoup.nodes.Document;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ScraperLifecycleTest {
    static class Adapter extends AbstractScraperAdapter<String> {
        protected String[] getEssentialSelectors(){return new String[0];}
        protected String[] getCardsSelectors(){return new String[0];}
        protected String buildUrl(String ticker){return "";}
        protected String executeSpecificScraping(Document d,String t){return "ok";}
    }
    @Test void selectorAlternativesShareOneBudgetAndUseAttachedState() {
        Page page=mock(Page.class);
        when(page.waitForSelector(anyString(),any())).thenAnswer(call -> {
            Page.WaitForSelectorOptions opts=call.getArgument(1);
            assertEquals(WaitForSelectorState.ATTACHED,opts.state);
            assertEquals(2000,opts.timeout);
            return mock(ElementHandle.class);
        });
        assertTrue(new Adapter().waitForAnySelector(page,new String[]{".hidden",".other"},2000,"SAPR11","url"));
        verify(page,times(1)).waitForSelector(eq(".hidden, .other"),any());
    }
    @Test void lifecycleErrorsAreNotSwallowedAsMissingTicker() {
        Page page=mock(Page.class);
        when(page.waitForSelector(anyString(),any())).thenThrow(new PlaywrightException("closed"));
        assertThrows(PlaywrightException.class,()->new Adapter().waitForAnySelector(page,new String[]{".a",".b"},2000,"PETR4","url"));
    }
    @Test void cancellationCleansExactlyOnceOnOwnerAfterWorkEnds() throws Exception {
        CountDownLatch started=new CountDownLatch(1), release=new CountDownLatch(1), cleaned=new CountDownLatch(1);
        AtomicInteger cleanups=new AtomicInteger(); AtomicReference<Thread> owner=new AtomicReference<>();
        AtomicReference<Thread> closer=new AtomicReference<>(); AtomicBoolean completed=new AtomicBoolean();
        var subscription=ScraperExecution.execute(()->{
            owner.set(Thread.currentThread()); started.countDown(); release.await(5,TimeUnit.SECONDS);
            completed.set(true); return "done";
        },()->{closer.set(Thread.currentThread());cleanups.incrementAndGet();cleaned.countDown();}).subscribe();
        assertTrue(started.await(3,TimeUnit.SECONDS)); subscription.dispose();
        assertEquals(0,cleanups.get()); release.countDown(); assertTrue(cleaned.await(3,TimeUnit.SECONDS));
        assertTrue(completed.get());assertEquals(1,cleanups.get());assertSame(owner.get(),closer.get());
    }
    @Test void queuedCancellationDoesNotStartWork() throws Exception {
        CountDownLatch started=new CountDownLatch(1), release=new CountDownLatch(1);
        var first=ScraperExecution.execute(()->{started.countDown();release.await(5,TimeUnit.SECONDS);return 1;},()->{}).subscribe();
        assertTrue(started.await(3,TimeUnit.SECONDS)); AtomicBoolean ran=new AtomicBoolean();
        var second=ScraperExecution.execute(()->{ran.set(true);return 2;},()->{}).subscribe();
        second.dispose();release.countDown();
        assertEquals(3,ScraperExecution.execute(()->3,()->{}).block(java.time.Duration.ofSeconds(3)));
        assertFalse(ran.get());first.dispose();
    }
    @Test void totalTimeoutIncludesQueueAndReturnsDomainError() {
        StepVerifier.withVirtualTime(()->ScraperExecution.bounded(reactor.core.publisher.Mono.never(),"SAPR11"))
                .thenAwait(java.time.Duration.ofSeconds(25))
                .expectError(br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException.class).verify();
    }
}
