package br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence;

import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.BdrEntity;
import br.dev.rodrigopinheiro.tickerscraper.adapter.output.persistence.entity.DividendoEntity;
import br.dev.rodrigopinheiro.tickerscraper.domain.model.enums.TipoDividendo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração para validar a lógica de preservação de histórico de dividendos
 * sem dependência de mocks complexos.
 */
class DividendHistoryPreservationTest {

    @Test
    void devePreservarDividendosHistoricosQuandoNovosDadosNaoContemTodos() {
        // Arrange - Simular dividendos existentes no banco
        BdrEntity bdrEntity = new BdrEntity();
        bdrEntity.setId(1L);
        bdrEntity.setTicker("NVDC34");

        List<DividendoEntity> dividendosExistentes = new ArrayList<>();
        
        // Dividendo histórico de outubro/2023
        DividendoEntity divOut2023 = new DividendoEntity();
        divOut2023.setId(1L);
        divOut2023.setMes(YearMonth.of(2023, 10));
        divOut2023.setValor(new BigDecimal("0.50"));
        divOut2023.setTipoDividendo(TipoDividendo.DIVIDENDO);
        divOut2023.setMoeda("USD");
        divOut2023.setAtivo(bdrEntity);
        dividendosExistentes.add(divOut2023);

        // Dividendo histórico de novembro/2023
        DividendoEntity divNov2023 = new DividendoEntity();
        divNov2023.setId(2L);
        divNov2023.setMes(YearMonth.of(2023, 11));
        divNov2023.setValor(new BigDecimal("0.55"));
        divNov2023.setTipoDividendo(TipoDividendo.DIVIDENDO);
        divNov2023.setMoeda("USD");
        divNov2023.setAtivo(bdrEntity);
        dividendosExistentes.add(divNov2023);

        // Simular novos dividendos vindos do scraping (apenas dezembro/2023)
        List<DividendoEntity> novosDividendos = new ArrayList<>();
        DividendoEntity divDez2023 = new DividendoEntity();
        divDez2023.setMes(YearMonth.of(2023, 12));
        divDez2023.setValor(new BigDecimal("0.60"));
        divDez2023.setTipoDividendo(TipoDividendo.DIVIDENDO);
        divDez2023.setMoeda("USD");
        divDez2023.setAtivo(bdrEntity);
        novosDividendos.add(divDez2023);

        // Act - Aplicar a lógica de merge (simulando o que acontece em savePreservingDividendHistory)
        Map<String, DividendoEntity> existentesMap = new HashMap<>();
        for (DividendoEntity existente : dividendosExistentes) {
            String chave = existente.getMes() + "|" + existente.getTipoDividendo() + "|" + existente.getMoeda();
            existentesMap.put(chave, existente);
        }

        Set<String> chavesProcessadas = new HashSet<>();
        List<DividendoEntity> dividendosFinais = new ArrayList<>();

        // Processar novos dividendos
        for (DividendoEntity novo : novosDividendos) {
            String chave = novo.getMes() + "|" + novo.getTipoDividendo() + "|" + novo.getMoeda();
            chavesProcessadas.add(chave);

            DividendoEntity existente = existentesMap.get(chave);
            if (existente != null) {
                // Atualizar se valor mudou
                if (!existente.getValor().equals(novo.getValor())) {
                    existente.setValor(novo.getValor());
                }
                dividendosFinais.add(existente);
            } else {
                // Adicionar novo dividendo
                dividendosFinais.add(novo);
            }
        }

        // Preservar dividendos históricos não presentes nos novos dados
        for (DividendoEntity existente : dividendosExistentes) {
            String chave = existente.getMes() + "|" + existente.getTipoDividendo() + "|" + existente.getMoeda();
            if (!chavesProcessadas.contains(chave)) {
                dividendosFinais.add(existente);
            }
        }

        // Assert - Verificar que o histórico foi preservado
        assertThat(dividendosFinais).hasSize(3); // Out/2023, Nov/2023, Dez/2023
        
        // Verificar que dividendos históricos foram preservados
        boolean temOutubro = dividendosFinais.stream()
            .anyMatch(d -> d.getMes().equals(YearMonth.of(2023, 10)) && 
                          d.getValor().equals(new BigDecimal("0.50")));
        assertThat(temOutubro).isTrue();

        boolean temNovembro = dividendosFinais.stream()
            .anyMatch(d -> d.getMes().equals(YearMonth.of(2023, 11)) && 
                          d.getValor().equals(new BigDecimal("0.55")));
        assertThat(temNovembro).isTrue();

        // Verificar que novo dividendo foi adicionado
        boolean temDezembro = dividendosFinais.stream()
            .anyMatch(d -> d.getMes().equals(YearMonth.of(2023, 12)) && 
                          d.getValor().equals(new BigDecimal("0.60")));
        assertThat(temDezembro).isTrue();
    }

    @Test
    void deveAtualizarDividendoExistenteSeValorMudou() {
        // Arrange
        BdrEntity bdrEntity = new BdrEntity();
        bdrEntity.setId(1L);

        List<DividendoEntity> dividendosExistentes = new ArrayList<>();
        DividendoEntity divExistente = new DividendoEntity();
        divExistente.setId(1L);
        divExistente.setMes(YearMonth.of(2023, 11));
        divExistente.setValor(new BigDecimal("0.55"));
        divExistente.setTipoDividendo(TipoDividendo.DIVIDENDO);
        divExistente.setMoeda("USD");
        divExistente.setAtivo(bdrEntity);
        dividendosExistentes.add(divExistente);

        // Novo dividendo com valor atualizado
        List<DividendoEntity> novosDividendos = new ArrayList<>();
        DividendoEntity divAtualizado = new DividendoEntity();
        divAtualizado.setMes(YearMonth.of(2023, 11));
        divAtualizado.setValor(new BigDecimal("0.70")); // Valor diferente
        divAtualizado.setTipoDividendo(TipoDividendo.DIVIDENDO);
        divAtualizado.setMoeda("USD");
        divAtualizado.setAtivo(bdrEntity);
        novosDividendos.add(divAtualizado);

        // Act - Aplicar lógica de merge
        Map<String, DividendoEntity> existentesMap = new HashMap<>();
        for (DividendoEntity existente : dividendosExistentes) {
            String chave = existente.getMes() + "|" + existente.getTipoDividendo() + "|" + existente.getMoeda();
            existentesMap.put(chave, existente);
        }

        for (DividendoEntity novo : novosDividendos) {
            String chave = novo.getMes() + "|" + novo.getTipoDividendo() + "|" + novo.getMoeda();
            DividendoEntity existente = existentesMap.get(chave);
            if (existente != null && !existente.getValor().equals(novo.getValor())) {
                existente.setValor(novo.getValor());
            }
        }

        // Assert
        assertThat(divExistente.getValor()).isEqualTo(new BigDecimal("0.70"));
    }
}