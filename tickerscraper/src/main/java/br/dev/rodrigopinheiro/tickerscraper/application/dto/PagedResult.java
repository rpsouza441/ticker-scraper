package br.dev.rodrigopinheiro.tickerscraper.application.dto;

import java.util.List;

public record PagedResult<T>(
        List<T> items,          // Os itens da página atual
        long totalElements,     // Total de itens em todas as páginas
        int totalPages,         // Número total de páginas
        int currentPage         // O número da página atual (base 0)
) {}
