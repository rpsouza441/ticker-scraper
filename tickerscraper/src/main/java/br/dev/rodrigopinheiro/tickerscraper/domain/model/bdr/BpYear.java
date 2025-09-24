package br.dev.rodrigopinheiro.tickerscraper.domain.model.bdr;

public class BpYear {

    private Integer ano;
    private AuditedValue ativosTotais;
    private AuditedValue passivosTotais;
    private AuditedValue dividaLongoPrazo;
    private AuditedValue pl;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public AuditedValue getAtivosTotais() {
        return ativosTotais;
    }

    public void setAtivosTotais(AuditedValue ativosTotais) {
        this.ativosTotais = ativosTotais;
    }

    public AuditedValue getPassivosTotais() {
        return passivosTotais;
    }

    public void setPassivosTotais(AuditedValue passivosTotais) {
        this.passivosTotais = passivosTotais;
    }

    public AuditedValue getDividaLongoPrazo() {
        return dividaLongoPrazo;
    }

    public void setDividaLongoPrazo(AuditedValue dividaLongoPrazo) {
        this.dividaLongoPrazo = dividaLongoPrazo;
    }

    public AuditedValue getPl() {
        return pl;
    }

    public void setPl(AuditedValue pl) {
        this.pl = pl;
    }
}
