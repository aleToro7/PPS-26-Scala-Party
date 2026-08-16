# 🚀 Scala Party - Control Room

> **Deadline ufficiale:** 10/10/2026 
> **Deadline interna**: \[Da concordare]
> **Team (45h/sprint tot.):** Federico Diotallevi, Alessandro Martini, Alessandro Torelli  
> **Processo:** SCRUM (Sprint 1 settimana | ~15h a persona/sprint)  
> **Repo Git:** [`[Link repository GitHub]`](https://github.com/aleToro7/Scala-Party.git)

---
# Stato dei Tasks
### 🔴 Da Fare (To Do)
```dataview
TABLE sprint AS "Sprint", assegnatario AS "Dev", punti AS "Story Points", data AS "Data Assegnazione"
WHERE contains(file.folder, "Gestione SCRUM/Tasks") AND stato = "todo"
SORT sprint ASC, sp DESC
```


### 🟡 In progress
```dataview
TABLE sprint AS "Sprint", assegnatario AS "Dev", punti AS "Story Points", data AS "Data Assegnazione"
WHERE contains(file.folder, "Gestione SCRUM/Tasks") AND stato = "in-progress"
SORT file.mtime DESC
```
### 🟠 In revisione
```dataview
TABLE sprint AS "Sprint", assegnatario AS "Dev", punti AS "Story Points", data AS "Data Assegnazione"
WHERE contains(file.folder, "Gestione SCRUM/Tasks") AND stato = "review"
```
### 🟢 Completati
```dataview
TABLE sprint AS "Sprint", assegnatario AS "Dev", punti AS "Story Points", data AS "Data Assegnazione"
WHERE contains(file.folder, "Gestione SCRUM/Tasks") AND stato = "done"
SORT file.mtime DESC
LIMIT 5
```

---
## 📌 Stato Moduli in Tempo Reale
### 🖥️ Server Engine ![[MOC-Server#Stato Modulo]] 
### 🎨 Client & Rendering ![[MOC-Client#Stato Modulo]]
### 🧩 Prolog Engine ![[MOC-Prolog-Engine#Stato Modulo]] 
--- 
## 🏛️ Registro Decisioni Tecniche (ADR)
```dataview
TABLE stato AS "Stato", data AS "Data Approvazione" WHERE contains(file.folder, "Architettura e Design/ADR") AND tipo = "ADR"
```

---

## 🏃 Sprint 1: Setup & Analisi Requisiti
**Periodo:** [17/08/2026] - [24/08/2026]  
**Capacità Target:** n Ore Totali (n SP)