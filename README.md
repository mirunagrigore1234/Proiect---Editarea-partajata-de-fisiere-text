# Editarea Partajată de Fișiere Text

## Descriere
O aplicație distribuită de tip client-server care permite mai multor utilizatori să vizualizeze și să editeze colaborativ fișiere text, asigurând acces exclusiv prin mecanisme de blocare. Aplicația este implementată utilizând Java, socket-uri TCP și programare concurentă (multi-threading).

Serverul gestionează un director de fișiere text, iar mai mulți clienți se pot conecta simultan pentru a:

- vizualiza fișiere
- prelua un fișier pentru editare
- salva modificările realizate

La un moment dat, **un singur client poate edita un fișier**.

---

## Tehnologii utilizate
- Java
- Sockets (TCP)
- Concurență (threads)
- I/O pentru fișiere
- Docker (pentru rularea serverului)

---

## Arhitectură

- Serverul gestionează:
  - conexiunile clienților (prin thread-uri)
  - starea fișierelor (liber / în editare)
- Clienții:
  - trimit comenzi către server
  - ascultă actualizări în mod asincron

Comunicarea se realizează prin socket-uri TCP, utilizând un protocol text-based simplu, bazat pe comenzi trimise de client și procesate de server.

---

## Funcționalități

### Conectare
- Clientul se autentifică printr-un nume
- Primește lista fișierelor disponibile + starea lor:
  - liber
  - în editare (și de către cine)

---

### Vizualizare fișier
- Orice client poate vizualiza conținutul unui fișier
- Dacă fișierul este editat → vizualizare read-only

---

### Editare fișier
- Clientul poate prelua un fișier dacă este liber
- Serverul:
  - marchează fișierul ca „în editare”
  - notifică ceilalți clienți

Dacă fișierul este deja în editare → cererea este refuzată

---

### Salvare modificări
- Clientul trimite noua versiune
- Serverul:
  - salvează pe disc
  - notifică ceilalți clienți
  - trimite conținutul actualizat

---

### Renunțare la editare
- Clientul eliberează fișierul
- Serverul notifică toți clienții

---

### Gestionare fișiere server
- Adăugare fișier → notificare către clienți
- Ștergere fișier → actualizare listă la clienți

---

## Exemplu utilizare

Client1:
EDIT notite.txt
[SERVER] editare permisa
[SERVER] Editing mode pentru notite.txt

Client2:
EDIT notite.txt
> [ERROR] fisierul este deja editat de miruna

---

## Protocol de comunicare

Comenzi disponibile:

- LIST
- VIEW <filename>
- EDIT <filename>
- SAVE <filename>
- CANCEL (eliberează fișierul)

---

## Rulare aplicație

### 1. Pornește serverul

```bash
java server.ServerMain
```

#### 2. Pornește clienți (în terminale separate)

```bash
java client.ClientMain
```

## Rulare cu Docker

Pentru a construi și rula serverul folosind Docker:

```bash
docker build -t text-editor-server .
docker run -p 1234:1234 text-editor-server
```

---

## Structura proiectului

```
src/
├── client/
│ ├── ClientMain.java
│ └── ServerListener.java
│
└── server/
├── ServerMain.java
├── ClientHandler.java
├── FileManager.java
└── FileWatcher.java

server_files/
└── example.txt

.gitignore
README.md
```
---

## Concurență și sincronizare
- Server concurent (multi-threaded)
- Acces exclusiv la editare
- Eliberare automată dacă clientul se deconectează

---

## Scenarii testate
- Conectarea mai multor clienți
- Vizualizarea simultană a unui fișier
- Editare exclusivă (un singur client)
- Refuz la editare concurentă
- Salvare și propagare modificări
- Eliberare fișier la renunțare
- Deconectare forțată și eliberare automată

---

## Limitări

- Nu există interfață grafică (doar CLI)
- Nu există un mecanism de versionare avansată
- Se transmite întreg conținutul fișierului (nu pe bază de diferențe – diff)

---

## Posibile îmbunătățiri

- Dezvoltarea unei interfețe grafice (JavaFX / Web)
- Implementarea actualizărilor pe bază de diferențe (diff), în locul transferului complet al fișierelor
- Introducerea unui mecanism de versionare a fișierelor
- Implementarea unui sistem de autentificare a utilizatorilor
- Dezvoltarea unor strategii de gestionare și rezolvare a conflictelor

---
  
## Concepte utilizate:
- programare concurentă
- comunicare client-server
- sincronizare și consistență
- lucrul cu fișiere

---
  
## Echipa

- Ana-Miruna Grigore  
- Mara-Catinca Marinescu
- Marica Maria Daria

**Profesor coordonator:** Ilie-Nemedi Iulian

---

## Note

Proiect realizat în cadrul unui curs universitar, având ca obiectiv implementarea unei aplicații distribuite de tip client-server pentru editarea partajată a fișierelor text.

Lucrarea evidențiază concepte precum programarea concurentă, comunicarea prin socket-uri, sincronizarea accesului la resurse și gestionarea consistenței între mai mulți clienți.

Proiectul are un scop educațional și este inclus în portofoliu.
