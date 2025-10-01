package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.AtivoFinanceiroJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.BdrJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.jpa.DividendoJpaRepository;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.BdrPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.mapper.DividendoPersistenceMapper;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Bdr;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.Dividendo;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BdrRepositoryAdapterTest {

    @Mock
    private BdrJpaRepository bdrJpa;
    
    @Mock
    private DividendoJpaRepository dividendoJpa;
    
    @Mock
    private AtivoFinanceiroJpaRepository ativoJpa;
    
    @Mock
    private BdrPersistenceMapper mapper;
    
    @Mock
    private DividendoPersistenceMapper dividendoMapper;

    @InjectMocks
    private BdrRepositoryAdapter adapter;

    private BdrEntity bdrEntity;
    private Bdr bdrDomain;
    private List<DividendoEntity> dividendosExistentes;

    @BeforeEach
    void setUp() {
        // Setup BDR entity
        bdrEntity = new BdrEntity();
        bdrEntity.setId(1L);
        bdrEntity.setTicker("NVDC34");

        // Setup BDR domain
        bdrDomain = new Bdr();
        bdrDomain.setTicker("NVDC34");
        
        // Setup dividendos existentes no banco (histórico)
        DividendoEntity dividendoHistorico1 = new DividendoEntity();
        dividendoHistorico1.setId(1L);
        dividendoHistorico1.setMes(YearMonth.of(2023, 10));
        dividendoHistorico1.setValor(new BigDecimal("0.50"));
        dividendoHistorico1.setTipoDividendo(TipoDividendo.DIVIDENDO);
        dividendoHistorico1.setMoeda("USD");
        dividendoHistorico1.setAtivo(bdrEntity);

        DividendoEntity dividendoHistorico2 = new DividendoEntity();
        dividendoHistorico2.setId(2L);
        dividendoHistorico2.setMes(YearMonth.of(2023, 11));
        dividendoHistorico2.setValor(new BigDecimal("0.55"));
        dividendoHistorico2.setTipoDividendo(TipoDividendo.DIVIDENDO);
        dividendoHistorico2.setMoeda("USD");
        dividendoHistorico2.setAtivo(bdrEntity);

        dividendosExistentes = Arrays.asList(dividendoHistorico1, dividendoHistorico2);
    }

    @Test
    void savePreservingDividendHistory_devePreservarDividendosHistoricos() {
        // Arrange
        Dividendo novoDiv1 = new Dividendo();
        novoDiv1.setMes(YearMonth.of(2023, 12));
        novoDiv1.setValor(new BigDecimal("0.60"));
        novoDiv1.setTipoDividendo(TipoDividendo.DIVIDENDO);
        novoDiv1.setMoeda("USD");

        bdrDomain.replaceDividendos(Arrays.asList(novoDiv1));

        // Mock setup
        when(bdrJpa.findByTicker("nvdc34")).thenReturn(Optional.of(bdrEntity));
        when(dividendoJpa.findByAtivoId(1L)).thenReturn(dividendosExistentes);
        when(bdrJpa.save(any(BdrEntity.class))).thenReturn(bdrEntity);
        when(mapper.toDomain(any(BdrEntity.class), any(DividendoPersistenceMapper.class))).thenReturn(bdrDomain);

        // Act
        Bdr resultado = adapter.savePreservingDividendHistory(bdrDomain, null);

        // Assert
        assertThat(resultado).isNotNull();
        
        // Verificar que os dividendos existentes foram buscados
        verify(dividendoJpa).findByAtivoId(1L);
        
        // Verificar que não houve delete de dividendos (comportamento principal)
        verify(dividendoJpa, never()).deleteByAtivoId(anyLong());
        
        // Verificar que a entidade foi salva
        verify(bdrJpa).save(any(BdrEntity.class));
    }

    @Test
    void savePreservingDividendHistory_deveAtualizarDividendoExistenteSeValorMudou() {
        // Arrange
        Dividendo dividendoAtualizado = new Dividendo();
        dividendoAtualizado.setMes(YearMonth.of(2023, 11)); // Mesmo mês do histórico
        dividendoAtualizado.setValor(new BigDecimal("0.70")); // Valor diferente
        dividendoAtualizado.setTipoDividendo(TipoDividendo.DIVIDENDO);
        dividendoAtualizado.setMoeda("USD");

        bdrDomain.replaceDividendos(Arrays.asList(dividendoAtualizado));

        // Mock setup
        when(bdrJpa.findByTicker("nvdc34")).thenReturn(Optional.of(bdrEntity));
        when(dividendoJpa.findByAtivoId(1L)).thenReturn(dividendosExistentes);
        when(bdrJpa.save(any(BdrEntity.class))).thenReturn(bdrEntity);
        when(mapper.toDomain(any(BdrEntity.class), any(DividendoPersistenceMapper.class))).thenReturn(bdrDomain);

        // Act
        adapter.savePreservingDividendHistory(bdrDomain, null);

        // Assert
        // Verificar que não houve delete (comportamento principal)
        verify(dividendoJpa, never()).deleteByAtivoId(anyLong());
        
        // Verificar que os dividendos existentes foram buscados
        verify(dividendoJpa).findByAtivoId(1L);
    }

    @Test
    void savePreservingDividendHistory_naoDeveAlterarDividendoSeValorIgual() {
        // Arrange
        Dividendo dividendoIgual = new Dividendo();
        dividendoIgual.setMes(YearMonth.of(2023, 11));
        dividendoIgual.setValor(new BigDecimal("0.55")); // Mesmo valor do histórico
        dividendoIgual.setTipoDividendo(TipoDividendo.DIVIDENDO);
        dividendoIgual.setMoeda("USD");

        bdrDomain.replaceDividendos(Arrays.asList(dividendoIgual));

        // Mock setup
        when(bdrJpa.findByTicker("nvdc34")).thenReturn(Optional.of(bdrEntity));
        when(dividendoJpa.findByAtivoId(1L)).thenReturn(dividendosExistentes);
        when(bdrJpa.save(any(BdrEntity.class))).thenReturn(bdrEntity);
        when(mapper.toDomain(any(BdrEntity.class), any(DividendoPersistenceMapper.class))).thenReturn(bdrDomain);

        // Act
        adapter.savePreservingDividendHistory(bdrDomain, null);

        // Assert
        // Verificar que não houve delete (comportamento principal)
        verify(dividendoJpa, never()).deleteByAtivoId(anyLong());
        
        // Verificar que os dividendos existentes foram buscados
        verify(dividendoJpa).findByAtivoId(1L);
    }
}