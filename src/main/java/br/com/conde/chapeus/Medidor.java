package br.com.conde.chapeus;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/** As tabelas do README, uma por argumento. */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String qual = args.length == 0 ? "tudo" : args[0];
        switch (qual) {
            case "estrategias" -> estrategias();
            case "balanca" -> balanca();
            case "otimo" -> otimo();
            case "contagem" -> contagem();
            case "derrotas" -> derrotas();
            default -> {
                estrategias();
                balanca();
                otimo();
                contagem();
                derrotas();
            }
        }
    }

    static void estrategias() {
        System.out.println("== quanto cada combinado ganha ==");
        System.out.println("pessoas  combinado        sorteios  vitorias   chance  acertos  erros  passes");
        for (int pessoas : new int[] {1, 3, 7, 15}) {
            for (Estrategia estrategia : new Estrategia[] {
                    Estrategias.hamming(pessoas), Estrategias.umSoChuta(),
                    Estrategias.todosChutam(), Estrategias.pelaMaioria(pessoas)}) {
                Varredura varredura = new Varredura(pessoas, estrategia);
                System.out.printf(Locale.ROOT, "%7d  %-15s %9d %9d %7.2f%% %8d %6d %7d%n",
                        pessoas, estrategia.nome(), varredura.sorteios(), varredura.vitorias(),
                        100 * varredura.chance(), varredura.acertos(), varredura.erros(),
                        varredura.passes());
            }
        }
    }

    static void balanca() {
        System.out.println("== acertos contra erros, em qualquer combinado ==");
        System.out.println("pessoas  combinado        acertos  erros  iguais?");
        for (int pessoas : new int[] {3, 7}) {
            for (Estrategia estrategia : new Estrategia[] {
                    Estrategias.hamming(pessoas), Estrategias.umSoChuta(),
                    Estrategias.todosChutam(), Estrategias.pelaMaioria(pessoas)}) {
                Varredura varredura = new Varredura(pessoas, estrategia);
                System.out.printf(Locale.ROOT, "%7d  %-15s %7d %6d %8s%n", pessoas,
                        estrategia.nome(), varredura.acertos(), varredura.erros(),
                        varredura.acertos() == varredura.erros() ? "sim" : "NAO");
            }
        }
        System.out.println();
        int quantas = 20_000;
        int desiguais = 0;
        long somaAcertos = 0;
        Random sorte = new Random(9);
        for (int i = 0; i < quantas; i++) {
            Varredura varredura = new Varredura(7, Estrategias.sorteada(7, sorte));
            if (varredura.acertos() != varredura.erros()) {
                desiguais++;
            }
            somaAcertos += varredura.acertos();
        }
        System.out.printf(Locale.ROOT,
                "%d combinados sorteados com 7 pessoas: %d com acertos diferentes de erros"
                        + " (%d acertos ao todo, media de %.2f por combinado)%n",
                quantas, desiguais, somaAcertos, (double) somaAcertos / quantas);
    }

    static void otimo() {
        System.out.println("== todos os combinados que existem ==");
        System.out.println("pessoas  times olhados  melhor  chance   Hamming");
        for (int pessoas = 1; pessoas <= ForcaBruta.LIMITE_DE_PESSOAS; pessoas++) {
            ForcaBruta.Resultado resultado = ForcaBruta.melhorDeTodas(pessoas);
            String hamming = "-";
            if (Integer.bitCount(pessoas + 1) == 1) {
                Varredura varredura = new Varredura(pessoas, Estrategias.hamming(pessoas));
                hamming = varredura.vitorias() + " de " + varredura.sorteios();
            }
            System.out.printf(Locale.ROOT, "%7d %14d %7d %7.2f%% %9s%n", pessoas,
                    resultado.timesOlhados(), resultado.melhorVitorias(),
                    100 * resultado.chance(), hamming);
        }
    }

    static void contagem() {
        System.out.println("== so olhando QUANTOS chapeus de cada cor a pessoa ve ==");
        System.out.println("pessoas  combinados  melhor por contagem   chance   Hamming");
        for (int pessoas = 1; pessoas <= 9; pessoas++) {
            ForcaBruta.Resultado resultado = ForcaBruta.melhorPorContagem(pessoas);
            String hamming = "-";
            if (Integer.bitCount(pessoas + 1) == 1) {
                hamming = String.format(Locale.ROOT, "%.2f%%",
                        100 * new Varredura(pessoas, Estrategias.hamming(pessoas)).chance());
            }
            System.out.printf(Locale.ROOT, "%7d %11d %20d %8.2f%% %9s%n", pessoas,
                    resultado.timesOlhados(), resultado.melhorVitorias(),
                    100 * resultado.chance(), hamming);
        }
    }

    static void derrotas() {
        int pessoas = 7;
        Varredura varredura = new Varredura(pessoas, Estrategias.hamming(pessoas));
        List<Integer> derrotas = varredura.derrotas();
        System.out.println("== os 16 sorteios que derrubam o time de 7 ==");
        System.out.println("sao " + derrotas.size() + " de " + varredura.sorteios()
                + ", e em cada um deles todas as " + pessoas + " pessoas falam e todas erram");
        System.out.println();
        for (int chapeus : derrotas) {
            StringBuilder cores = new StringBuilder();
            for (int pessoa = 0; pessoa < pessoas; pessoa++) {
                cores.append((chapeus >> pessoa) & 1);
            }
            System.out.printf(Locale.ROOT, "   %s  falam %d, acertam %d%n", cores,
                    varredura.quemFala(chapeus), acertosNo(varredura, chapeus));
        }
        System.out.println();
        System.out.println("o primeiro deles, pessoa por pessoa:");
        System.out.print(varredura.escrito(derrotas.get(1)));
        System.out.println();
        System.out.println("e um sorteio qualquer que nao esta na lista:");
        System.out.print(varredura.escrito(0b0000001));
    }

    private static int acertosNo(Varredura varredura, int chapeus) {
        int certos = 0;
        for (int pessoa = 0; pessoa < varredura.pessoas(); pessoa++) {
            int palpite = varredura.estrategia().palpite(pessoa, chapeus & ~(1 << pessoa));
            if (palpite != Estrategia.PASSA && palpite == ((chapeus >> pessoa) & 1)) {
                certos++;
            }
        }
        return certos;
    }
}
