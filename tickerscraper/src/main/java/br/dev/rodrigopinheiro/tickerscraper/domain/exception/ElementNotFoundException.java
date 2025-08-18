package br.dev.rodrigopinheiro.tickerscraper.domain.exception;

/**
 * Exceção lançada quando um elemento essencial não é encontrado durante o scraping.
 * Esta exceção é usada para indicar falhas críticas na extração de dados.
 */
public class ElementNotFoundException extends RuntimeException {

    /**
     * Cria uma nova exceção com uma mensagem detalhada.
     *
     * @param message A mensagem de erro
     */
    public ElementNotFoundException(String message) {
        super(message);
    }

    /**
     * Cria uma nova exceção com uma mensagem detalhada e a causa raiz.
     *
     * @param message A mensagem de erro
     * @param cause A causa raiz da exceção
     */
    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Cria uma nova exceção para um seletor específico que falhou.
     *
     * @param selector O seletor CSS que não encontrou nenhum elemento
     * @return Uma nova instância de ElementNotFoundException
     */
    public static ElementNotFoundException forSelector(String selector) {
        return new ElementNotFoundException("Elemento não encontrado com o seletor: " + selector);
    }

    /**
     * Cria uma nova exceção para múltiplos seletores que falharam.
     *
     * @param selectors Os seletores CSS que não encontraram nenhum elemento
     * @return Uma nova instância de ElementNotFoundException
     */
    public static ElementNotFoundException forSelectors(String... selectors) {
        return new ElementNotFoundException("Elemento não encontrado com os seletores: " + String.join(", ", selectors));
    }
}