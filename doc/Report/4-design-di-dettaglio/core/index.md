# Modulo Core

In questo capitolo viene approfondita la progettazione di dettaglio del modulo **core**, illustrando l'organizzazione modulare dei package, la modellazione delle strutture dati, i design pattern adottati e gli algoritmi matematici che governano la simulazione di gioco.

## Organizzazione del Codice e Struttura dei Package

Il modulo `core` è organizzato secondo una struttura in cui ciascun package racchiude una precisa area di responsabilità:

```text
com.unibo.scalaparty.core
├── dto
├── ecs
│   └── systems
├── engine
│   └── input
├── geometry
└── model
    └── map
```

La suddivisione risponde ai seguenti criteri di separazione delle responsabilità:

- **`core.engine`**: Contiene la facciata principale del motore di gioco e il sottosistema di elaborazione degli input (`core.engine.input`). Questo package è l'unico necessario per iniziare la simulazione e aggiornare lo stato del mondo di gioco dall'esterno del modulo.
- **`core.ecs`**: Definisce il cuore del modello dell'Entity-Component-System: contiene l'astrazione del mondo, i componenti e le entità.
- **`core.ecs.systems`**: Raggruppa la famiglia dei sistemi che compongono la pipeline di simulazione, ciascuno specializzato su un singolo aspetto fisico o di gameplay.
- **`core.geometry`**: Fornisce le funzioni e le strutture dati matematiche necessarie per la modellazione della geometria 2D.
- **`core.model`**: Modella i concetti statici e le configurazioni del dominio come le impostazioni di gioco, gli eventi, la mappa di gioco e l'output della partita.
- **`core.dto`**: Definisce i contratti di trasferimento dati verso l'esterno.
