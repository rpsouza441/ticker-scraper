package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper;

import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlaywrightInitializer {
    private static final Logger log = LoggerFactory.getLogger(PlaywrightInitializer.class);

    private Playwright playwright;
    private Browser browser;

    @PostConstruct
    public void init() {
        boolean headless = !"false".equalsIgnoreCase(System.getenv("PW_HEADLESS")); // default true
        String userDataDir = System.getenv("PW_USER_DATA_DIR"); // opcional

        List<String> args = new ArrayList<>(List.of(
                "--no-sandbox",
                "--disable-blink-features=AutomationControlled"
        ));
        // exemplo: PW_ARGS="--disable-web-security,--lang=pt-BR"
        String extraArgs = System.getenv("PW_ARGS");
        if (extraArgs != null && !extraArgs.isBlank()) {
            for (String a : extraArgs.split(",")) args.add(a.trim());
        }

        playwright = Playwright.create();

        BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(args);

        if (userDataDir != null && !userDataDir.isBlank()) {
            // Contexto persistente → abre com perfil (cookies/cache). Útil se você quiser “amolecer” anti-bot
            browser = playwright.chromium().launchPersistentContext(
                    Path.of(userDataDir), new BrowserType.LaunchPersistentContextOptions()
                            .setHeadless(headless)
                            .setArgs(args)
            ).browser();
            log.info("Playwright inicializado (persistente) headless={} userDataDir={}", headless, userDataDir);
        } else {
            browser = playwright.chromium().launch(opts);
            log.info("Playwright inicializado headless={} args={}", headless, args);
        }
    }

    public Browser getBrowser() {
        return browser;
    }

    @PreDestroy
    public void shutdown() {
        try { if (browser != null) browser.close(); } catch (Exception ignored) {}
        try { if (playwright != null) playwright.close(); } catch (Exception ignored) {}
        log.info("Playwright finalizado.");
    }
}
