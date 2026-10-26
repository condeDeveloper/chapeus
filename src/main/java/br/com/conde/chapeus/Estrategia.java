package br.com.conde.chapeus;

/**
 * O combinado do time, antes de os chapeus serem postos.
 *
 * <p>O jogo: cada pessoa ganha um chapeu de uma de duas cores, ve o de todo mundo
 * menos o seu e, ao mesmo tempo que as outras, diz uma cor ou passa. O time ganha
 * se pelo menos uma pessoa falar e ninguem que falou errar.
 *
 * <p>Ninguem conversa depois de os chapeus entrarem. A unica coisa combinada e esta
 * funcao, e ela e a mesma para todo mundo o tempo todo.
 */
public interface Estrategia {

    /** O que a pessoa diz quando resolve nao arriscar. */
    int PASSA = -1;

    String nome();

    /**
     * O que a pessoa fala.
     *
     * @param pessoa quem esta falando, de 0 a n-1
     * @param vistos os chapeus de todo mundo, com o bit desta pessoa sempre em 0,
     *     porque e exatamente o que ela nao sabe
     * @return 0, 1 ou {@link #PASSA}
     */
    int palpite(int pessoa, int vistos);
}
