package br.com.conde.chapeus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os numeros que estao escritos no README, cada um preso a uma medida. */
class MedidasTest {

    @Test
    @DisplayName("README: a tabela dos combinados")
    void aTabelaDosCombinados() {
        int[][] esperado = {
            // pessoas, Hamming, um so chuta, todos chutam, pela maioria
            {1, 1, 1, 1, 0},
            {3, 6, 4, 1, 2},
            {7, 112, 64, 1, 2},
            {15, 30_720, 16_384, 1, 2},
        };
        for (int[] linha : esperado) {
            int pessoas = linha[0];
            Estrategia[] estrategias = {Estrategias.hamming(pessoas), Estrategias.umSoChuta(),
                Estrategias.todosChutam(), Estrategias.pelaMaioria(pessoas)};
            for (int i = 0; i < estrategias.length; i++) {
                assertThat(new Varredura(pessoas, estrategias[i]).vitorias())
                        .as(pessoas + " pessoas, " + estrategias[i].nome())
                        .isEqualTo(linha[i + 1]);
            }
        }
    }

    @Test
    @DisplayName("README: Hamming com 7 pessoas da 112 acertos e 112 erros")
    void osNumerosDeSete() {
        Varredura varredura = new Varredura(7, Estrategias.hamming(7));
        assertThat(varredura.sorteios()).isEqualTo(128);
        assertThat(varredura.vitorias()).isEqualTo(112);
        assertThat(varredura.acertos()).isEqualTo(112);
        assertThat(varredura.erros()).isEqualTo(112);
        assertThat(varredura.passes()).isEqualTo(672);
        assertThat(varredura.derrotas()).hasSize(16);
    }

    @Test
    @DisplayName("README: nos 20.000 combinados sorteados, acertos e erros empatam em todos")
    void aBalancaNaoCede() {
        Random sorte = new Random(9);
        int desiguais = 0;
        long somaAcertos = 0;
        for (int i = 0; i < 20_000; i++) {
            Varredura varredura = new Varredura(7, Estrategias.sorteada(7, sorte));
            if (varredura.acertos() != varredura.erros()) {
                desiguais++;
            }
            somaAcertos += varredura.acertos();
        }
        assertThat(desiguais).isZero();
        assertThat(somaAcertos).as("e a soma de acertos e exatamente esta").isEqualTo(5_973_887);
    }

    @Test
    @DisplayName("README: a forca bruta olhou 531.441 times e nao achou nada melhor que 75%")
    void aForcaBruta() {
        assertThat(ForcaBruta.melhorDeTodas(1).melhorVitorias()).isEqualTo(1);
        assertThat(ForcaBruta.melhorDeTodas(1).timesOlhados()).isEqualTo(3);
        assertThat(ForcaBruta.melhorDeTodas(2).melhorVitorias()).isEqualTo(2);
        assertThat(ForcaBruta.melhorDeTodas(2).timesOlhados()).isEqualTo(81);
        assertThat(ForcaBruta.melhorDeTodas(3).melhorVitorias()).isEqualTo(6);
        assertThat(ForcaBruta.melhorDeTodas(3).timesOlhados()).isEqualTo(531_441);
    }

    @Test
    @DisplayName("README: a tabela de quem so olha a quantidade")
    void aTabelaPorContagem() {
        int[][] esperado = {
            // pessoas, combinados, melhor
            {1, 3, 1}, {2, 9, 2}, {3, 27, 6}, {4, 81, 11}, {5, 243, 22},
            {6, 729, 42}, {7, 2187, 85}, {8, 6561, 170}, {9, 19_683, 342},
        };
        for (int[] linha : esperado) {
            ForcaBruta.Resultado resultado = ForcaBruta.melhorPorContagem(linha[0]);
            assertThat(resultado.timesOlhados()).as(linha[0] + " pessoas")
                    .isEqualTo(linha[1]);
            assertThat(resultado.melhorVitorias()).as(linha[0] + " pessoas")
                    .isEqualTo(linha[2]);
        }
    }

    @Test
    @DisplayName("README: olhar a quantidade piora com gente a mais; olhar quem e quem melhora")
    void asDuasCurvas() {
        double porContagemTres = ForcaBruta.melhorPorContagem(3).chance();
        double porContagemSete = ForcaBruta.melhorPorContagem(7).chance();
        assertThat(porContagemSete).isLessThan(porContagemTres);
        assertThat(new Varredura(7, Estrategias.hamming(7)).chance())
                .isGreaterThan(new Varredura(3, Estrategias.hamming(3)).chance());
    }

    @Test
    @DisplayName("README: as 16 derrotas sao palavras do codigo e todas as 7 pessoas erram nelas")
    void asDezesseisDerrotas() {
        Varredura varredura = new Varredura(7, Estrategias.hamming(7));
        assertThat(varredura.derrotas()).hasSize(16);
        assertThat(varredura.derrotas()).contains(0, 0b1111111);
        for (int chapeus : varredura.derrotas()) {
            assertThat(varredura.quemFala(chapeus)).isEqualTo(7);
            int soma = 0;
            for (int pessoa = 0; pessoa < 7; pessoa++) {
                if ((chapeus & (1 << pessoa)) != 0) {
                    soma ^= pessoa + 1;
                }
            }
            assertThat(soma).isZero();
        }
    }
}
