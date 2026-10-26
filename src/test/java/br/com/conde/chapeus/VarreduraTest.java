package br.com.conde.chapeus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VarreduraTest {

    @Test
    @DisplayName("Hamming ganha n de n+1, para 1, 3, 7 e 15 pessoas")
    void aChanceDeHamming() {
        for (int pessoas : new int[] {1, 3, 7, 15}) {
            Varredura varredura = new Varredura(pessoas, Estrategias.hamming(pessoas));
            assertThat(varredura.chance()).as(pessoas + " pessoas")
                    .isCloseTo((double) pessoas / (pessoas + 1), within(1e-12));
        }
    }

    @Test
    @DisplayName("as derrotas de Hamming sao exatamente as palavras do codigo")
    void asDerrotasSaoAsPalavrasDoCodigo() {
        for (int pessoas : new int[] {1, 3, 7, 15}) {
            Varredura varredura = new Varredura(pessoas, Estrategias.hamming(pessoas));
            int bits = Integer.numberOfTrailingZeros(pessoas + 1);
            assertThat(varredura.derrotas()).as(pessoas + " pessoas")
                    .hasSize(1 << (pessoas - bits));
            for (int chapeus : varredura.derrotas()) {
                // Palavra do codigo: o ou-exclusivo dos numeros de quem esta de
                // chapeu 1 da zero. Esta conta nao passa por nenhuma estrategia.
                int soma = 0;
                for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                    if ((chapeus & (1 << pessoa)) != 0) {
                        soma ^= pessoa + 1;
                    }
                }
                assertThat(soma).as("sorteio " + Integer.toBinaryString(chapeus)).isZero();
                assertThat(varredura.quemFala(chapeus)).as("todo mundo fala na derrota")
                        .isEqualTo(pessoas);
            }
        }
    }

    @Test
    @DisplayName("quando Hamming ganha, uma pessoa so fala")
    void naVitoriaUmaSoFala() {
        int pessoas = 7;
        Varredura varredura = new Varredura(pessoas, Estrategias.hamming(pessoas));
        var derrotas = varredura.derrotas();
        int vitorias = 0;
        for (int chapeus = 0; chapeus < varredura.sorteios(); chapeus++) {
            if (derrotas.contains(chapeus)) {
                continue;
            }
            vitorias++;
            assertThat(varredura.quemFala(chapeus))
                    .as("sorteio " + Integer.toBinaryString(chapeus)).isEqualTo(1);
        }
        assertThat(vitorias).isEqualTo(112);
    }

    @Test
    @DisplayName("acertos e erros empatam em todo combinado, sorteado ou nao")
    void acertosEErrosEmpatam() {
        for (int pessoas : new int[] {1, 3, 7}) {
            for (Estrategia estrategia : new Estrategia[] {Estrategias.hamming(pessoas),
                    Estrategias.umSoChuta(), Estrategias.todosChutam(),
                    Estrategias.pelaMaioria(pessoas)}) {
                Varredura varredura = new Varredura(pessoas, estrategia);
                assertThat(varredura.acertos()).as(pessoas + ", " + estrategia.nome())
                        .isEqualTo(varredura.erros());
            }
        }
        Random sorte = new Random(4);
        for (int i = 0; i < 300; i++) {
            Varredura varredura = new Varredura(6, Estrategias.sorteada(6, sorte));
            assertThat(varredura.acertos()).as("combinado sorteado " + i)
                    .isEqualTo(varredura.erros());
        }
    }

    @Test
    @DisplayName("todo palpite dado e acerto, erro ou passe, sem sobra")
    void aContaFecha() {
        Varredura varredura = new Varredura(7, Estrategias.pelaMaioria(7));
        assertThat(varredura.acertos() + varredura.erros() + varredura.passes())
                .isEqualTo(7 * varredura.sorteios());
    }

    @Test
    @DisplayName("todos chutam ganha uma vez so, qualquer que seja a turma")
    void todosChutam() {
        for (int pessoas : new int[] {1, 3, 5, 7}) {
            assertThat(new Varredura(pessoas, Estrategias.todosChutam()).vitorias())
                    .as(pessoas + " pessoas").isEqualTo(1);
        }
    }

    @Test
    @DisplayName("um so chuta ganha sempre metade, quanta gente tiver")
    void umSoChuta() {
        for (int pessoas : new int[] {1, 3, 7, 15}) {
            Varredura varredura = new Varredura(pessoas, Estrategias.umSoChuta());
            assertThat(varredura.chance()).as(pessoas + " pessoas").isCloseTo(0.5, within(1e-12));
        }
    }

    @Test
    @DisplayName("o sorteio sai escrito pessoa por pessoa")
    void escrito() {
        Varredura varredura = new Varredura(3, Estrategias.hamming(3));
        assertThat(varredura.escrito(0)).contains("pessoa 1: chapeu 0, diz 1 (errado)");
        assertThat(varredura.escrito(0b001)).contains("passa");
    }

    @Test
    @DisplayName("turma vazia ou grande demais e recusada")
    void limites() {
        assertThatThrownBy(() -> new Varredura(0, Estrategias.todosChutam()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Varredura(23, Estrategias.todosChutam()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
