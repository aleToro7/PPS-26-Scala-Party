---
tipo: meeting
sprint: <% tp.file.title %>
data: <% tp.date.now("YYYY-MM-DD") %>
---
<%* // Sposta automaticamente la nota appena creata nella cartella dei verbali
await tp.file.move("Università/Paradigmi di Programmazione e Sviluppo/Progetto Scala Party/Gestione SCRUM/Sprint Logs/Sprint " + tp.file.title);
-%>

# 📅 Sprint Planning - <% tp.file.title %>

* **Data:** <% tp.date.now("YYYY-MM-DD") %>  
* **Presenti:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli  

---

## 🎯 Obiettivo dello Sprint
- [ ] 

## 📋 Task Assegnati (Story Points)
| Task ID | Descrizione | Assegnatario | SP  | Stato |
| :------ | :---------- | :----------- | :-: | :---: |
|         |             |              |     |  ``   |

## 💬 Note & Decisioni
- 

## 🚫 Impedimenti (Blockers)
- Nessuno