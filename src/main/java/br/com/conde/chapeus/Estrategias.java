package br.com.conde.chapeus;

import java.util.Random;

/** Os combinados que este repositorio mede. */
public final class Estrategias {

    private Estrategias() {
    }

    /**
     * O combinado do codigo de Hamming, que so existe quando a turma tem
     * {@code 2^k - 1} pessoas: 1, 3, 7, 15, 31...
     *
     * <p>Cada pessoa numera a si mesma de 1 a n e soma, em ou-exclusivo, os numeros
     * de quem esta de chapeu 1. Se der zero, o chapeu dela faria a soma fechar em
     * zero caso fosse 0, entao ela chuta 1. Se der o proprio numero, o chapeu dela
     * faria a soma fechar em zero caso fosse 1, entao ela chuta 0. Nos outros casos
     * ela passa.
     *
     * <p>Em outras palavras: todo mundo aposta que o sorteio NAO deu uma palavra do
     * codigo. Quando deu, todo mundo fala e todo mundo erra, junto. Quando nao deu,
     * uma pessoa so fala, e acerta.
     */
    public static Estrategia hamming(int pessoas) {
        if (Integer.bitCount(pessoas + 1) != 1) {
            throw new IllegalArgumentException(
                    "o codigo de Hamming pede 2^k - 1 pessoas, e nao " + pessoas);
        }
        return new Estrategia() {

            @Override
            public String nome() {
                return "Hamming";
            }

            @Override
            public int palpite(int pessoa, int vistos) {
                int soma = 0;
                for (int outro = 0; outro < pessoas; outro++) {
                    if ((vistos & (1 << outro)) != 0) {
                        soma ^= outro + 1;
                    }
                }
                if (soma == 0) {
                    return 1;
                }
                if (soma == pessoa + 1) {
                    return 0;
                }
                return PASSA;
            }
        };
    }

    /** Uma pessoa chuta sempre, e as outras passam sempre. Meio a meio, e so. */
    public static Estrategia umSoChuta() {
        return new Estrategia() {

            @Override
            public String nome() {
                return "um so chuta";
            }

            @Override
            public int palpite(int pessoa, int vistos) {
                return pessoa == 0 ? 0 : PASSA;
            }
        };
    }

    /** Todo mundo chuta a mesma cor. Precisa de muita sorte ao mesmo tempo. */
    public static Estrategia todosChutam() {
        return new Estrategia() {

            @Override
            public String nome() {
                return "todos chutam";
            }

            @Override
            public int palpite(int pessoa, int vistos) {
                return 0;
            }
        };
    }

    /**
     * Cada pessoa chuta a cor que ela ve mais vezes, e passa no empate. Parece
     * esperteza e nao e: o chapeu de ninguem sabe o que os outros estao fazendo.
     */
    public static Estrategia pelaMaioria(int pessoas) {
        return new Estrategia() {

            @Override
            public String nome() {
                return "pela maioria";
            }

            @Override
            public int palpite(int pessoa, int vistos) {
                int uns = Integer.bitCount(vistos);
                int zeros = pessoas - 1 - uns;
                if (uns == zeros) {
                    return PASSA;
                }
                return uns > zeros ? 1 : 0;
            }
        };
    }

    /**
     * Um combinado escrito numa tabela: {@code tabela[pessoa][vistos]} ja diz o que
     * falar. E assim que a forca bruta percorre todos os combinados que existem.
     */
    public static Estrategia deTabela(String nome, int[][] tabela) {
        return new Estrategia() {

            @Override
            public String nome() {
                return nome;
            }

            @Override
            public int palpite(int pessoa, int vistos) {
                return tabela[pessoa][vistos];
            }
        };
    }

    /** Um combinado sorteado, para medir o que vale para qualquer um deles. */
    public static Estrategia sorteada(int pessoas, Random sorte) {
        int[][] tabela = new int[pessoas][1 << pessoas];
        for (int pessoa = 0; pessoa < pessoas; pessoa++) {
            for (int vistos = 0; vistos < (1 << pessoas); vistos++) {
                tabela[pessoa][vistos] = sorte.nextInt(3) - 1;
            }
        }
        return deTabela("sorteada", tabela);
    }
}
