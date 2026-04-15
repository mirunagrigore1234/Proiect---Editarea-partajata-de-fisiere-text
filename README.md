# Editarea Partajată de Fișiere Text

## Descriere
Această aplicație implementează un sistem distribuit de tip **client-server** pentru editarea partajată a fișierelor text.

Serverul gestionează un director cu fișiere text, iar mai mulți clienți se pot conecta pentru a:
- vizualiza fișiere
- prelua un fișier în editare
- salva modificări

La un moment dat, **un singur client poate edita un fișier**.

---

## Tehnologii utilizate
- Java
- Sockets (TCP)
- Concurență (threads)
- I/O pentru fișiere
- Docker (pentru rularea serverului)

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

## Concurență și sincronizare
- Server concurent (multi-threaded)
- Acces exclusiv la editare
- Eliberare automată dacă clientul se deconectează

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

## Rulare aplicație

### 1. Pornește serverul

```bash
java server.ServerMain
```

#### 2. Pornește clienți (în terminale separate)

```bash
java client.ClientMain
```

## Scenarii testate
- Conectarea mai multor clienți
- Vizualizarea simultană a unui fișier
- Editare exclusivă (un singur client)
- Refuz la editare concurentă
- Salvare și propagare modificări
- Eliberare fișier la renunțare
- Deconectare forțată și eliberare automată

## Limitări
- Nu există interfață grafică (CLI only)
- Nu există versionare avansată
- Se trimite întreg conținutul fișierului (nu diff)
  
## Conceopte utilizate:
- programare concurentă
- comunicare client-server
- sincronizare și consistență
- lucrul cu fișiere
  
## Echipa

- Ana-Miruna Grigore  
- Mara-Catinca Marinescu
- Marica Maria Daria

**Profesor coordonator:** Ilie-Nemedi Iulian

## Note

Proiect realizat în cadrul unui curs universitar, având ca obiectiv implementarea unei aplicații distribuite de tip client-server pentru editarea partajată a fișierelor text.

Lucrarea evidențiază concepte precum programarea concurentă, comunicarea prin socket-uri, sincronizarea accesului la resurse și gestionarea consistenței între mai mulți clienți.

Proiectul are un scop educațional și este inclus în portofoliu.
