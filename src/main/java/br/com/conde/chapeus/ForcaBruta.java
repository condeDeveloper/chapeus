package br.com.conde.chapeus;

/**
 * Percorre combinados em vez de sorteios: em vez de perguntar "esse combinado
 * ganha quanto", pergunta "existe combinado melhor?".
 *
 * <p>E a unica resposta honesta para "da para melhorar". Dizer que uma estrategia
 * e boa porque ganha 87,5% nao diz nada se ninguem olhou as outras.
 *
 * <p>O preco e brutal: com n pessoas, cada uma tem 3 elevado a 2^(n-1) combinados,
 * e o time tem isso elevado a n. Com 3 pessoas sao 531.441 times; com 4, dois
 * quatrilhoes.
 */
public final class ForcaBruta {

    /** O melhor que apareceu, e quantos times foram olhados para saber disso. */
    public record Resultado(int pessoas, int melhorVitorias, int sorteios, long timesOlhados,
            int[][] tabela) {

        public double chance() {
            return (double) melhorVitorias / sorteios;
        }

        public Estrategia estrategia() {
            return Estrategias.deTabela("a melhor de todas", tabela);
        }
    }

    private ForcaBruta() {
    }

    /** Acima disso o numero de times nao cabe em lugar nenhum. */
    public static final int LIMITE_DE_PESSOAS = 3;

    /**
     * Olha TODOS os combinados possiveis de um time e devolve o melhor.
     */
    public static Resultado melhorDeTodas(int pessoas) {
        if (pessoas < 1 || pessoas > LIMITE_DE_PESSOAS) {
            throw new IllegalArgumentException("todos os combinados so ate "
                    + LIMITE_DE_PESSOAS + " pessoas, e foram pedidas " + pessoas);
        }
        int observacoes = 1 << (pessoas - 1);
        int porPessoa = (int) potencia(3, observacoes);
        int sorteios = 1 << pessoas;
        int[] codigos = new int[pessoas];
        int[][] tabela = new int[pessoas][sorteios];
        int[][] melhorTabela = new int[pessoas][sorteios];
        int melhor = -1;
        long olhados = 0;
        while (true) {
            for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                montaTabela(tabela[pessoa], pessoa, pessoas, codigos[pessoa]);
            }
            int vitorias = conta(tabela, pessoas, sorteios);
            olhados++;
            if (vitorias > melhor) {
                melhor = vitorias;
                for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                    melhorTabela[pessoa] = tabela[pessoa].clone();
                }
            }
            int qual = 0;
            while (qual < pessoas) {
                codigos[qual]++;
                if (codigos[qual] < porPessoa) {
                    break;
                }
                codigos[qual] = 0;
                qual++;
            }
            if (qual == pessoas) {
                break;
            }
        }
        return new Resultado(pessoas, melhor, sorteios, olhados, melhorTabela);
    }

    /**
     * Olha so os combinados em que a pessoa decide pela QUANTIDADE de chapeus de
     * cada cor que ela ve, sem olhar quem esta com qual.
     *
     * <p>E a familia de combinados que parece natural, e e justamente a que nao
     * chega perto do Hamming: o que importa nao e quantos voce ve, e quais.
     */
    public static Resultado melhorPorContagem(int pessoas) {
        if (pessoas < 1 || pessoas > 9) {
            throw new IllegalArgumentException("por contagem so ate 9 pessoas");
        }
        int sorteios = 1 << pessoas;
        int[] regra = new int[pessoas];
        int[][] tabela = new int[pessoas][sorteios];
        int[][] melhorTabela = new int[pessoas][sorteios];
        int melhor = -1;
        long olhados = 0;
        while (true) {
            for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                for (int vistos = 0; vistos < sorteios; vistos++) {
                    tabela[pessoa][vistos] = regra[Integer.bitCount(vistos & ~(1 << pessoa))] - 1;
                }
            }
            int vitorias = conta(tabela, pessoas, sorteios);
            olhados++;
            if (vitorias > melhor) {
                melhor = vitorias;
                for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                    melhorTabela[pessoa] = tabela[pessoa].clone();
                }
            }
            int qual = 0;
            while (qual < pessoas) {
                regra[qual]++;
                if (regra[qual] < 3) {
                    break;
                }
                regra[qual] = 0;
                qual++;
            }
            if (qual == pessoas) {
                break;
            }
        }
        return new Resultado(pessoas, melhor, sorteios, olhados, melhorTabela);
    }

    /** Quantos sorteios esse time ganha. */
    private static int conta(int[][] tabela, int pessoas, int sorteios) {
        int vitorias = 0;
        for (int chapeus = 0; chapeus < sorteios; chapeus++) {
            boolean falou = false;
            boolean errou = false;
            for (int pessoa = 0; pessoa < pessoas && !errou; pessoa++) {
                int palpite = tabela[pessoa][chapeus & ~(1 << pessoa)];
                if (palpite == Estrategia.PASSA) {
                    continue;
                }
                falou = true;
                if (palpite != ((chapeus >> pessoa) & 1)) {
                    errou = true;
                }
            }
            if (falou && !errou) {
                vitorias++;
            }
        }
        return vitorias;
    }

    /**
     * Escreve o combinado da pessoa a partir do seu numero.
     *
     * <p>So as observacoes em que o bit dela e 0 importam: as outras nunca
     * acontecem, porque ela nunca ve o proprio chapeu.
     */
    private static void montaTabela(int[] tabela, int pessoa, int pessoas, int codigo) {
        int sorteios = 1 << pessoas;
        int resto = codigo;
        for (int vistos = 0; vistos < sorteios; vistos++) {
            if ((vistos & (1 << pessoa)) != 0) {
                tabela[vistos] = Estrategia.PASSA;
                continue;
            }
            tabela[vistos] = resto % 3 - 1;
            resto /= 3;
        }
    }

    private static long potencia(long base, int vezes) {
        long fora = 1;
        for (int i = 0; i < vezes; i++) {
            fora *= base;
        }
        return fora;
    }
}
