package br.com.conde.chapeus;

import java.util.ArrayList;
import java.util.List;

/**
 * Passa um combinado por TODOS os sorteios de chapeu possiveis.
 *
 * <p>Sao 2^n sorteios e nenhum e mais provavel que o outro, entao a conta de
 * quantas vezes o time ganha e uma contagem inteira: nada de amostra, nada de
 * simulacao, nada de arredondamento.
 */
public final class Varredura {

    private final int pessoas;
    private final Estrategia estrategia;
    private final int sorteios;
    private final int vitorias;
    private final int acertos;
    private final int erros;
    private final int passes;
    private final List<Integer> derrotas = new ArrayList<>();

    public Varredura(int pessoas, Estrategia estrategia) {
        if (pessoas < 1 || pessoas > 22) {
            throw new IllegalArgumentException("pessoas fora de 1..22: " + pessoas);
        }
        this.pessoas = pessoas;
        this.estrategia = estrategia;
        this.sorteios = 1 << pessoas;
        int ganhou = 0;
        int certos = 0;
        int errados = 0;
        int passou = 0;
        for (int chapeus = 0; chapeus < sorteios; chapeus++) {
            boolean alguemFalou = false;
            boolean alguemErrou = false;
            for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                int vistos = chapeus & ~(1 << pessoa);
                int palpite = estrategia.palpite(pessoa, vistos);
                if (palpite == Estrategia.PASSA) {
                    passou++;
                    continue;
                }
                alguemFalou = true;
                int verdade = (chapeus >> pessoa) & 1;
                if (palpite == verdade) {
                    certos++;
                } else {
                    errados++;
                    alguemErrou = true;
                }
            }
            if (alguemFalou && !alguemErrou) {
                ganhou++;
            } else {
                derrotas.add(chapeus);
            }
        }
        this.vitorias = ganhou;
        this.acertos = certos;
        this.erros = errados;
        this.passes = passou;
    }

    public int pessoas() {
        return pessoas;
    }

    public Estrategia estrategia() {
        return estrategia;
    }

    /** Quantos sorteios de chapeu existem: 2 elevado a quantidade de gente. */
    public int sorteios() {
        return sorteios;
    }

    public int vitorias() {
        return vitorias;
    }

    public double chance() {
        return (double) vitorias / sorteios;
    }

    /** Quantos palpites certos foram dados, somando todas as pessoas e todos os sorteios. */
    public int acertos() {
        return acertos;
    }

    public int erros() {
        return erros;
    }

    public int passes() {
        return passes;
    }

    /** Os sorteios em que o time perdeu. */
    public List<Integer> derrotas() {
        return List.copyOf(derrotas);
    }

    /** Quantas pessoas falam nesse sorteio. */
    public int quemFala(int chapeus) {
        int quantas = 0;
        for (int pessoa = 0; pessoa < pessoas; pessoa++) {
            if (estrategia.palpite(pessoa, chapeus & ~(1 << pessoa)) != Estrategia.PASSA) {
                quantas++;
            }
        }
        return quantas;
    }

    /** O sorteio escrito em cores, com o que cada pessoa falou. */
    public String escrito(int chapeus) {
        StringBuilder texto = new StringBuilder();
        for (int pessoa = 0; pessoa < pessoas; pessoa++) {
            int vistos = chapeus & ~(1 << pessoa);
            int palpite = estrategia.palpite(pessoa, vistos);
            int verdade = (chapeus >> pessoa) & 1;
            String fala = palpite == Estrategia.PASSA ? "passa" : "diz " + palpite;
            String nota = palpite == Estrategia.PASSA ? "" : palpite == verdade ? " (certo)"
                    : " (errado)";
            texto.append(String.format("   pessoa %d: chapeu %d, %s%s%n",
                    pessoa + 1, verdade, fala, nota));
        }
        return texto.toString();
    }
}
