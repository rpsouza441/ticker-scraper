package br.dev.rodrigopinheiro.tickerscraper.domain.model.enums;

public enum ParidadeMethod {
    HTML,          // página principal
    API,           // Brapi/depósito/csv-oficial
    MANUAL,        // override humano
    OCR,           // PDF/RDI do depositário parseado
    LEGACY_CACHE   // base interna antiga
}
