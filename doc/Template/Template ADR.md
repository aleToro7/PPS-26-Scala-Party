---
tipo: adr
stato: in-review
data: 2026-08-12
modulo: <%* let modulo = await tp.system.prompt("Modulo coinvolto (es. server, client, prolog, common):"); -%>
sprint: Template ADR
punti: 3
assegnatario: Alessandro
priorita: alta
categoria:
---
<%*
await tp.file.move("Università/Paradigmi di Programmazione e Sviluppo/Progetto Scala Party/Architettura e Design/ADR/ADR-" + tp.file.title);  
-%>
# 🏛️ <% tp.file.title %>

* **Data:** <% tp.date.now("YYYY-MM-DD") %>
* **Autori:** Federico Diotallevi, Alessandro Martini, Alessandro Torelli
* **Moduli Coinvolti:** [[MOC-<% tp.frontmatter.modulo %>]]

---

### 📝 Contesto & Problema
Descrivere il problema o la necessità tecnica che ha portato a questa decisione.

### 💡 Decisione
Spiegare la soluzione scelta (es. uso di *tuProlog* ad eventi discreti).

### ⚖️ Conseguenze
* **Positive:** 
* **Negative / Rischi:**