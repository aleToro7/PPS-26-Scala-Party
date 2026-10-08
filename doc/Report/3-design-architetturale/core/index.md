# Modulo Core

Il modulo _core_ costituisce il nucleo computazionale di ScalaParty. È stato ideato come modulo separato per essere completamente slegato da dipendenze esterne, operando come libreria pura ed indipendente, eseguibile utilizzando come unica dipendenza la libreria standard di Scala 3.

## Il mondo come macchina a stati

L'intero ciclo di vita di una partita è modellato attorno all'idea di un'evoluzione discreta nel tempo del mondo di gioco. Per "mondo" si intende lo stato corrente del gioco, cioè l'insieme di tutti le entità attive ed degli eventi generati in quel preciso istante. Questa modellazione rappresenta il mondo a tutti gli effetti come una macchina a stati deterministica, la cui funzione di transizione è la seguente:

$$\text{World}_{t'} = \delta(\text{World}_t, \text{Input}, \Delta t)$$

Dove:
- $\text{World}_{t}$ rappresenta la fotografia del mondo di gioco all'istante $t$.
- $\text{Input}$ è l'elenco degli inviate dei comandi inviati dai giocatori durante l'intervallo temporale.
- $\Delta t$ è l'intervallo di tempo trascorso tra $t$ e $t'$, ossia il tempo trascorso dall'ultimo aggiornamento.

La definizione dell'evoluzione come un insieme di stati determinati da input e tempo trascorso permette di definire l'intera partita come una pipeline su cui eseguono vari filtri che evolvono e trasformano il mondo. Questo approccio rende una partita completamente riproducibile: data un mondo iniziale e la sequenza temporale dei comandi inviati, l'evoluzione del mondo è perfettamente deterministica.
