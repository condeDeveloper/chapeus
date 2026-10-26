package br.com.conde.chapeus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ForcaBrutaTest {

    @Test
    @DisplayName("com tres pessoas, nenhum dos 531.441 combinados ganha mais que 6 de 8")
    void ninguemBateHamming() {
        ForcaBruta.Resultado resultado = ForcaBruta.melhorDeTodas(3);
        assertThat(resultado.timesOlhados()).isEqualTo(531_441);
        assertThat(resultado.melhorVitorias()).isEqualTo(6);
        assertThat(resultado.chance()).isEqualTo(0.75);
        assertThat(new Varredura(3, Estrategias.hamming(3)).vitorias())
                .as("e Hamming chega la").isEqualTo(6);
    }

    @Test
    @DisplayName("a melhor estrategia que a forca bruta acha ganha o que ela disse que ganha")
    void aMelhorFuncionaMesmo() {
        for (int pessoas = 1; pessoas <= 3; pessoas++) {
            ForcaBruta.Resultado resultado = ForcaBruta.melhorDeTodas(pessoas);
            Varredura varredura = new Varredura(pessoas, resultado.estrategia());
            assertThat(varredura.vitorias()).as(pessoas + " pessoas")
                    .isEqualTo(resultado.melhorVitorias());
        }
    }

    @Test
    @DisplayName("com duas pessoas nao da para fazer melhor que com uma")
    void duasPessoasNaoAjudam() {
        assertThat(ForcaBruta.melhorDeTodas(1).chance()).isEqualTo(0.5);
        assertThat(ForcaBruta.melhorDeTodas(2).chance()).isEqualTo(0.5);
        assertThat(ForcaBruta.melhorDeTodas(2).timesOlhados()).isEqualTo(81);
    }

    @Test
    @DisplayName("acima de tres pessoas a forca bruta se recusa, porque nao cabe")
    void limite() {
        assertThatThrownBy(() -> ForcaBruta.melhorDeTodas(4))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("3 pessoas");
        assertThatThrownBy(() -> ForcaBruta.melhorPorContagem(10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("olhar so a quantidade empata com Hamming em tres pessoas e perde feio em sete")
    void porContagem() {
        assertThat(ForcaBruta.melhorPorContagem(3).melhorVitorias()).isEqualTo(6);
        ForcaBruta.Resultado sete = ForcaBruta.melhorPorContagem(7);
        assertThat(sete.timesOlhados()).isEqualTo(2187);
        assertThat(sete.melhorVitorias()).isEqualTo(85);
        assertThat(new Varredura(7, Estrategias.hamming(7)).vitorias()).isEqualTo(112);
    }

    @Test
    @DisplayName("olhar so a quantidade piora quando entra a quarta pessoa")
    void aQuartaPessoaAtrapalha() {
        assertThat(ForcaBruta.melhorPorContagem(3).chance()).isEqualTo(0.75);
        assertThat(ForcaBruta.melhorPorContagem(4).chance()).isEqualTo(11.0 / 16);
        assertThat(ForcaBruta.melhorPorContagem(4).chance())
                .isLessThan(ForcaBruta.melhorPorContagem(3).chance());
    }

    @Test
    @DisplayName("a melhor por contagem tambem ganha o que foi dito, refeita pela varredura")
    void porContagemConfere() {
        for (int pessoas = 1; pessoas <= 7; pessoas++) {
            ForcaBruta.Resultado resultado = ForcaBruta.melhorPorContagem(pessoas);
            assertThat(new Varredura(pessoas, resultado.estrategia()).vitorias())
                    .as(pessoas + " pessoas").isEqualTo(resultado.melhorVitorias());
        }
    }
}
