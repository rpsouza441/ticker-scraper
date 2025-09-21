package br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf;

import br.dev.rodrigopinheiro.tickerscraper.application.port.output.EtfScraperPort;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfDadosFinanceirosDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfInfoCardsDTO;
import br.dev.rodrigopinheiro.tickerscraper.infrastructure.scraper.etf.dto.EtfInfoHeaderDTO;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scraper para dados de ETFs do site Investidor10.
 * Implementa parsing específico baseado no HTML fornecido.
 */
@Component
@Qualifier("etfPlaywrightScraper")
public class EtfScraper implements EtfScraperPort {

    private static final Logger logger = LoggerFactory.getLogger(EtfScraper.class);
    
    private static final String BASE_URL = "https://investidor10.com.br/etfs/";
    private static final Pattern VALOR_PATTERN = Pattern.compile("R\\$\\s*([\\d.,]+)");
    private static final Pattern PERCENTUAL_PATTERN = Pattern.compile("([\\d.,]+)%");
    private static final Pattern BILHAO_PATTERN = Pattern.compile("([\\d.,]+)\\s*B");
    private static final Pattern TRILHAO_PATTERN = Pattern.compile("([\\d.,]+)\\s*T");
    private static final Pattern MILHAO_PATTERN = Pattern.compile("([\\d.,]+)\\s*M");

    @Override
    public Mono<EtfDadosFinanceirosDTO> scrapeEtfData(String ticker) {
        return Mono.fromCallable(() -> {
            logger.info("Iniciando scraping para ETF: {}", ticker);
            
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                Page page = browser.newPage();
                
                String url = BASE_URL + ticker.toLowerCase();
                logger.debug("Acessando URL: {}", url);
                
                page.navigate(url);
                page.waitForLoadState();
                
                // Extrai dados do header
                EtfInfoHeaderDTO headerData = extractHeaderData(page, ticker);
                
                // Extrai dados dos cards
                EtfInfoCardsDTO cardsData = extractCardsData(page);
                
                EtfDadosFinanceirosDTO result = new EtfDadosFinanceirosDTO(headerData, cardsData);
                
                logger.info("Scraping concluído com sucesso para ETF: {}", ticker);
                return result;
                
            } catch (Exception e) {
                logger.error("Erro durante scraping do ETF {}: {}", ticker, e.getMessage(), e);
                throw new RuntimeException("Falha no scraping do ETF: " + ticker, e);
            }
        });
    }

    /**
     * Extrai dados do header (ticker e nome do ETF).
     */
    private EtfInfoHeaderDTO extractHeaderData(Page page, String ticker) {
        try {
            // Extrai ticker do h1
            String tickerFromPage = page.locator("#header_action .name-ticker h1").textContent().trim();
            
            // Extrai nome do ETF do h2
            String nomeEtf = page.locator("#header_action .name-ticker h2.name-company").textContent().trim();
            
            logger.debug("Header extraído - Ticker: {}, Nome: {}", tickerFromPage, nomeEtf);
            
            return new EtfInfoHeaderDTO(tickerFromPage, nomeEtf);
            
        } catch (Exception e) {
            logger.warn("Erro ao extrair dados do header para {}: {}", ticker, e.getMessage());
            return new EtfInfoHeaderDTO(ticker, "Nome não disponível");
        }
    }

    /**
     * Extrai dados dos cards de informações financeiras.
     */
    private EtfInfoCardsDTO extractCardsData(Page page) {
        try {
            logger.debug("Extraindo dados dos cards...");
            
            // Valor atual - primeiro card com classe "cotacao"
            String valorAtual = page.locator("#cards-ticker ._card.cotacao ._card-body span.value").textContent().trim();
            
            // Capitalização - segundo card
            String capitalizacao = page.locator("#cards-ticker ._card:nth-child(2) ._card-body span").textContent().trim();
            
            // Variação 12M - terceiro card
            String variacao12M = page.locator("#cards-ticker ._card:nth-child(3) ._card-body span").textContent().trim();
            
            // Variação 60M - quarto card
            String variacao60M = page.locator("#cards-ticker ._card:nth-child(4) ._card-body span").textContent().trim();
            
            // DY - quinto card com classe "dy"
            String dy = page.locator("#cards-ticker ._card.dy ._card-body span").textContent().trim();
            
            logger.debug("Cards extraídos - Valor: {}, Cap: {}, Var12M: {}, Var60M: {}, DY: {}", 
                        valorAtual, capitalizacao, variacao12M, variacao60M, dy);
            
            return new EtfInfoCardsDTO(valorAtual, capitalizacao, variacao12M, variacao60M, dy);
            
        } catch (Exception e) {
            logger.error("Erro ao extrair dados dos cards: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na extração dos dados dos cards", e);
        }
    }

    /**
     * Converte texto monetário para BigDecimal (ex: "R$ 142,70" -> 142.70).
     */
    private BigDecimal parseValorMonetario(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Matcher matcher = VALOR_PATTERN.matcher(texto);
        if (matcher.find()) {
            String valor = matcher.group(1).replace(".", "").replace(",", ".");
            return new BigDecimal(valor);
        }
        
        logger.warn("Não foi possível extrair valor monetário de: {}", texto);
        return BigDecimal.ZERO;
    }

    /**
     * Converte texto percentual para BigDecimal (ex: "11,88%" -> 11.88).
     */
    private BigDecimal parsePercentual(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Matcher matcher = PERCENTUAL_PATTERN.matcher(texto);
        if (matcher.find()) {
            String valor = matcher.group(1).replace(",", ".");
            return new BigDecimal(valor);
        }
        
        logger.warn("Não foi possível extrair percentual de: {}", texto);
        return BigDecimal.ZERO;
    }

    /**
     * Converte capitalização com sufixos para BigDecimal normalizado.
     * Ex: "9,78 B" -> 9780000000, "1,5 T" -> 1500000000000, "500 M" -> 500000000
     */
    private BigDecimal parseCapitalizacao(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Remove "R$" se presente
        texto = texto.replace("R$", "").trim();
        
        // Verifica trilhões
        Matcher trilhaoMatcher = TRILHAO_PATTERN.matcher(texto);
        if (trilhaoMatcher.find()) {
            String valor = trilhaoMatcher.group(1).replace(",", ".");
            return new BigDecimal(valor).multiply(new BigDecimal("1000000000000"));
        }
        
        // Verifica bilhões
        Matcher bilhaoMatcher = BILHAO_PATTERN.matcher(texto);
        if (bilhaoMatcher.find()) {
            String valor = bilhaoMatcher.group(1).replace(",", ".");
            return new BigDecimal(valor).multiply(new BigDecimal("1000000000"));
        }
        
        // Verifica milhões
        Matcher milhaoMatcher = MILHAO_PATTERN.matcher(texto);
        if (milhaoMatcher.find()) {
            String valor = milhaoMatcher.group(1).replace(",", ".");
            return new BigDecimal(valor).multiply(new BigDecimal("1000000"));
        }
        
        // Tenta parsing direto como valor monetário
        return parseValorMonetario("R$ " + texto);
    }
}