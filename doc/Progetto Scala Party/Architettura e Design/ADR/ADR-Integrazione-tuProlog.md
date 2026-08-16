---
tipo: ADR
stato: Da valutare
data: 2026-08-13
---
# 🏛️ Uso di tuProlog ad Eventi Discreti

* **Autori:** Alessandro T., Federico D., Alessandro M.

### Decisione
Per preservare i 60 FPS del Game Loop in Scala sul server, tuProlog verrà invocato unicamente ad **eventi discreti** (spawn power-up, decisioni periodiche dei Bot IA).