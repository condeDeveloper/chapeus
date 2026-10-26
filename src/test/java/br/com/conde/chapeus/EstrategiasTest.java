package br.com.conde.chapeus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EstrategiasTest {

    @Test
    @DisplayName("o combinado de Hamming so existe para 1, 3, 7, 15... pessoas")
    void hammingPedeUmaTurmaCerta() {
        for (int pessoas : new int[] {1, 3, 7, 15, 31}) {
            assertThat(Estrategias.hamming(pessoas)).isNotNull();
        }
        for (int pessoas : new int[] {2, 4, 5, 6, 8}) {
            assertThatThrownBy(() -> Estrategias.hamming(pessoas))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("2^k - 1");
        }
    }

    @Test
    @DisplayName("a regra de Hamming tem tres casos: soma zero, soma igual ao proprio numero, e o resto")
    void osTresCasos() {
        Estrategia hamming = Estrategias.hamming(3);
        // Ninguem de chapeu 1: a soma da zero, entao todo mundo chuta 1.
        assertThat(hamming.palpite(0, 0b000)).isEqualTo(1);
        assertThat(hamming.palpite(1, 0b000)).isEqualTo(1);
        // A pessoa 1 ve a 2 e a 3 de chapeu 1: soma 2 xor 3 = 1, que e o numero dela.
        assertThat(hamming.palpite(0, 0b110)).isZero();
        // A pessoa 1 ve so a 2 de chapeu 1: soma 2, que nao e 0 nem 1.
        assertThat(hamming.palpite(0, 0b010)).isEqualTo(Estrategia.PASSA);
    }

    @Test
    @DisplayName("quando um so chuta, so a primeira pessoa fala")
    void umSoChuta() {
        Estrategia estrategia = Estrategias.umSoChuta();
        assertThat(estrategia.palpite(0, 0b1010)).isZero();
        for (int pessoa = 1; pessoa < 4; pessoa++) {
            assertThat(estrategia.palpite(pessoa, 0b1010)).isEqualTo(Estrategia.PASSA);
        }
    }

    @Test
    @DisplayName("quando todos chutam, ninguem passa nunca")
    void todosChutam() {
        for (int pessoa = 0; pessoa < 5; pessoa++) {
            assertThat(Estrategias.todosChutam().palpite(pessoa, pessoa))
                    .isNotEqualTo(Estrategia.PASSA);
        }
    }

    @Test
    @DisplayName("pela maioria, o empate faz passar")
    void pelaMaioria() {
        Estrategia estrategia = Estrategias.pelaMaioria(5);
        assertThat(estrategia.palpite(0, 0b11110)).as("quatro uns").isEqualTo(1);
        assertThat(estrategia.palpite(0, 0b00000)).as("quatro zeros").isZero();
        assertThat(estrategia.palpite(0, 0b00110)).as("dois e dois")
                .isEqualTo(Estrategia.PASSA);
    }

    @Test
    @DisplayName("o combinado de tabela fala o que esta escrito nela")
    void deTabela() {
        int[][] tabela = new int[2][4];
        tabela[0][2] = 1;
        tabela[1][1] = Estrategia.PASSA;
        Estrategia estrategia = Estrategias.deTabela("teste", tabela);
        assertThat(estrategia.nome()).isEqualTo("teste");
        assertThat(estrategia.palpite(0, 2)).isEqualTo(1);
        assertThat(estrategia.palpite(1, 1)).isEqualTo(Estrategia.PASSA);
    }

    @Test
    @DisplayName("o combinado sorteado so fala 0, 1 ou passa")
    void sorteada() {
        Estrategia estrategia = Estrategias.sorteada(4, new java.util.Random(1));
        for (int pessoa = 0; pessoa < 4; pessoa++) {
            for (int vistos = 0; vistos < 16; vistos++) {
                assertThat(estrategia.palpite(pessoa, vistos)).isBetween(-1, 1);
            }
        }
    }
}
