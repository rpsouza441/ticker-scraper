package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.base;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import br.dev.rodrigopinheiro.tickerscraper.domain.exception.ScrapingTimeoutException;

/** Owns the shared Playwright connection. Cancellation never closes a live page
 * from another thread or interrupts its message loop. */
public final class ScraperExecution {
    public static final Duration BUDGET = Duration.ofSeconds(25);
    private static final ThreadPoolExecutor OWNER = new ThreadPoolExecutor(1, 1, 0,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(8), runnable -> {
                Thread thread = new Thread(runnable, "scraper-browser-owner");
                thread.setDaemon(true);
                return thread;
            });
    private ScraperExecution() {}

    public static <T> Mono<T> execute(Callable<T> work, Runnable cleanup) {
        return Mono.defer(() -> {
            Map<String, String> context = MDC.getCopyOfContextMap();
            return Mono.create(sink -> {
                AtomicBoolean cancelled = new AtomicBoolean();
                Runnable job = () -> {
                    if (cancelled.get()) return;
                    if (context != null) MDC.setContextMap(context);
                    T value = null;
                    Throwable failure = null;
                    try { value = work.call(); }
                    catch (Exception ex) { failure = ex; }
                    finally {
                        try { cleanup.run(); }
                        catch (Exception ex) { if (failure == null) failure = ex; }
                        MDC.clear();
                    }
                    if (!cancelled.get()) {
                        if (failure != null) sink.error(failure);
                        else sink.success(value);
                    }
                };
                sink.onCancel(() -> { cancelled.set(true); OWNER.remove(job); });
                try { OWNER.execute(job); }
                catch (RejectedExecutionException ex) { sink.error(ex); }
            });
        });
    }

    public static <T> Mono<T> bounded(Mono<T> work, String ticker) {
        return work.timeout(BUDGET, Mono.error(new ScrapingTimeoutException(
                ticker, "", BUDGET, "TOTAL_SCRAPING_BUDGET")));
    }
}
