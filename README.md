# chapeus

O problema dos chapéus em Java 21, com todos os sorteios varridos e todos os combinados de
três pessoas conferidos um por um.

O jogo: cada pessoa ganha um chapéu de uma de duas cores, vê o de todo mundo menos o seu e,
ao mesmo tempo que as outras, diz uma cor ou passa. O time ganha se pelo menos uma pessoa
falar e ninguém que falou errar. Combina-se tudo antes; depois que os chapéus entram, não se
conversa mais.

Com sete pessoas, existe um combinado que ganha **87,5%** das vezes. E o motivo não é o que
parece.

## O que tem aqui

- `Estrategia`: o combinado, que é só uma função de (quem sou eu, o que estou vendo) para
  (digo 0, digo 1, passo).
- `Estrategias`: o combinado do código de Hamming, mais três que servem de régua: um só
  chuta, todos chutam, e cada um chuta a cor que vê mais.
- `Varredura`: passa um combinado por **todos** os 2^n sorteios de chapéu. Nada de
  simulação: é contagem inteira sobre um domínio finito.
- `ForcaBruta`: percorre **combinados** em vez de sorteios, para responder a única pergunta
  que importa de verdade: existe combinado melhor?
- `Medidor`: as tabelas.

## Como rodar

```bash
mvn -B verify
mvn -q exec:java -Dexec.args="estrategias"  # quanto cada combinado ganha
mvn -q exec:java -Dexec.args="balanca"      # acertos contra erros
mvn -q exec:java -Dexec.args="otimo"        # todos os combinados que existem
mvn -q exec:java -Dexec.args="contagem"     # só olhando quantos chapéus de cada cor
mvn -q exec:java -Dexec.args="derrotas"     # os 16 sorteios que derrubam o time de 7
```

## Seis achados

### 1. A estratégia boa não acerta mais. Ela erra junto.

```
pessoas  combinado        acertos  erros  iguais?
      7  Hamming             112    112      sim
      7  um so chuta          64     64      sim
      7  todos chutam        448    448      sim
      7  pela maioria        308    308      sim
```

Com sete pessoas e o combinado de Hamming, somando todos os palpites de todas as pessoas em
todos os 128 sorteios: **112 acertos e 112 erros**. Empate exato. Com o combinado burro de
todo mundo chutar: 448 e 448. Empate exato de novo.

Rodei 20.000 combinados **sorteados** procurando um que quebrasse o empate. Nenhum quebrou.

Isso acontece porque cada palpite individual, olhado sobre todos os sorteios, acerta
exatamente metade das vezes: a pessoa não sabe nada sobre o próprio chapéu, e nada que ela
veja muda isso. Nenhum combinado pode aumentar o total de acertos. O que um bom combinado faz
é **arrumar os erros**:

- Hamming erra 112 vezes, mas espremidas em **16 sorteios**, com 7 erros cada.
- E acerta 112 vezes, espalhadas em **112 sorteios**, com 1 acerto cada.

Mesmo número dos dois lados. Em 112 dias o time ganha e em 16 perde feio. A estratégia não
melhora a sorte de ninguém; ela combina os fracassos no mesmo dia.

### 2. Com duas pessoas, não dá para fazer melhor do que com uma

```
pessoas  times olhados  melhor  chance   Hamming
      1              3       1   50.00%    1 de 2
      2             81       2   50.00%         -
      3         531441       6   75.00%    6 de 8
```

Com uma pessoa, 50%: ela chuta e acabou. Com **duas**, os 81 combinados que existem foram
olhados um por um, e nenhum passa de 50%. A segunda pessoa não vale nada.

Com três, salta para 75%.

### 3. Ninguém bate os 75% com três pessoas

A linha de baixo da tabela acima é o resultado que vale o repositório. São 3 opções para cada
uma das 4 coisas que uma pessoa pode ver, logo 81 combinados por pessoa, logo **531.441**
times possíveis. Todos foram montados e medidos.

O melhor ganha 6 de 8. É exatamente o que o código de Hamming dá.

Um aviso de honestidade: isso está **medido** para três pessoas. Que 7/8 seja o melhor
possível com sete, este repositório não prova — ele mostra que Hamming chega lá e que a
família de combinados que parece natural não chega nem perto.

### 4. O que importa não é quantos você vê, é quais

A família de combinados em que cada pessoa decide só pela **quantidade** de chapéus de cada
cor que enxerga, sem olhar quem está com qual:

```
pessoas  combinados  melhor por contagem   chance   Hamming
      1           3                    1    50.00%    50.00%
      2           9                    2    50.00%         -
      3          27                    6    75.00%    75.00%
      4          81                   11    68.75%         -
      5         243                   22    68.75%         -
      6         729                   42    65.63%         -
      7        2187                   85    66.41%    87.50%
      8        6561                  170    66.41%         -
      9       19683                  342    66.80%         -
```

Com três pessoas essa família empata com Hamming, nos 75%. Depois ela **desce**: a quarta
pessoa piora o time, e de sete em diante fica parada em uns 66%, enquanto Hamming vai a 87,5%
com sete e a 93,75% com quinze.

Não é falta de gente nem falta de informação. É a informação errada: a quantidade de chapéus
de cada cor joga fora exatamente a parte que permite combinar os erros no mesmo sorteio.

### 5. As dezesseis derrotas não são aleatórias

```
   0000000  falam 7, acertam 0
   1110000  falam 7, acertam 0
   1001100  falam 7, acertam 0
   0111100  falam 7, acertam 0
   0101010  falam 7, acertam 0
   1011010  falam 7, acertam 0
   1100110  falam 7, acertam 0
   0010110  falam 7, acertam 0
   1101001  falam 7, acertam 0
   0011001  falam 7, acertam 0
   0100101  falam 7, acertam 0
   1010101  falam 7, acertam 0
   1000011  falam 7, acertam 0
   0110011  falam 7, acertam 0
   0001111  falam 7, acertam 0
   1111111  falam 7, acertam 0
```

São as 16 palavras do código de Hamming de 7 bits, e nada mais. O teste confere isso sem
passar por estratégia nenhuma: em todas elas, o ou-exclusivo dos números das pessoas de
chapéu 1 dá zero.

Num sorteio de derrota, as sete pessoas falam e as sete erram:

```
   pessoa 1: chapeu 1, diz 0 (errado)
   pessoa 2: chapeu 1, diz 0 (errado)
   pessoa 3: chapeu 1, diz 0 (errado)
   pessoa 4: chapeu 0, diz 1 (errado)
   pessoa 5: chapeu 0, diz 1 (errado)
   pessoa 6: chapeu 0, diz 1 (errado)
   pessoa 7: chapeu 0, diz 1 (errado)
```

Em qualquer um dos outros 112, uma pessoa só fala, e acerta:

```
   pessoa 1: chapeu 1, diz 1 (certo)
   pessoa 2: chapeu 0, passa
   pessoa 3: chapeu 0, passa
   pessoa 4: chapeu 0, passa
   pessoa 5: chapeu 0, passa
   pessoa 6: chapeu 0, passa
   pessoa 7: chapeu 0, passa
```

### 6. Esperteza individual é o pior combinado da tabela

```
pessoas  combinado        sorteios  vitorias   chance  acertos  erros  passes
      7  Hamming               128       112   87.50%      112    112     672
      7  um so chuta           128        64   50.00%       64     64     768
      7  todos chutam          128         1    0.78%      448    448       0
      7  pela maioria          128         2    1.56%      308    308     280
```

"Cada um chuta a cor que vê mais, e passa no empate" parece o combinado sensato, e é o
penúltimo da tabela: **2 vitórias em 128**. Pior que a pessoa sozinha chutando no escuro, que
ganha 64.

Cada uma delas está fazendo o melhor que pode sozinha, e é por isso que o time perde: todas
erram no mesmo sorteio por acidente, em vez de erraram no mesmo sorteio de propósito.

## O que as medidas me corrigiram

**Eu ia escrever que o combinado de Hamming "acerta mais".** Não acerta: 112 a 112, contra
112 a 112 de qualquer outro combinado que dê o mesmo número de palpites. Rodei 20.000
combinados sorteados esperando achar pelo menos um desequilíbrio, e vieram **zero**. Depois
disso a razão ficou óbvia, porque nenhum palpite pode saber nada sobre o próprio chapéu. Mas
eu só fui atrás da razão depois de o número me contrariar, e a frase do README mudou de "ganha
mais vezes" para "erra junto", que é o que o repositório mede.

**A força bruta de quatro pessoas não cabe, e isso melhorou o repositório.** São 3 elevado a 8
combinados por pessoa, elevado a 4 pessoas: 1,8 quatrilhão de times. Em vez de desistir, troquei
a pergunta: "qual a melhor de todas" virou "qual a melhor entre as que só olham a quantidade".
A resposta mede outra coisa, mais interessante que a original, e virou o achado 4. Quando a
busca não cabe, trocar a pergunta costuma render mais que forçar a busca.

**Eu esperava que a família "por contagem" melhorasse com mais gente.** Ela piora: 75% com
três, 68,75% com quatro, 66% com sete. Se eu tivesse medido só n=3, teria concluído o
contrário do certo, porque é exatamente em n=3 que ela empata com o ótimo. Uma tabela com uma
linha só não é uma tabela.

**"Pela maioria" eu pus na lista como régua razoável.** Ficou em penúltimo, com 2 vitórias em
128, atrás de uma pessoa sozinha chutando no escuro. Era para ser a estratégia de controle e
virou um achado.

## Como está organizado

```
src/main/java/br/com/conde/chapeus/
  Estrategia.java   o combinado: de (quem sou, o que vejo) para (0, 1, passo)
  Estrategias.java  Hamming e as tres reguas
  Varredura.java    um combinado contra todos os 2^n sorteios
  ForcaBruta.java   todos os combinados que existem, para n ate 3
  Medidor.java      as tabelas
src/test/java/br/com/conde/chapeus/
  EstrategiasTest, VarreduraTest, ForcaBrutaTest, MedidasTest
```

30 testes. O `MedidasTest` prende cada número deste README a uma medida, inclusive os 5.973.887
acertos somados dos 20.000 combinados sorteados, que é determinístico porque a semente está
fixada.

## Licença

MIT.
