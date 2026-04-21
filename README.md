# Shared Text File Editing

## Description
A distributed client-server application that allows multiple users to collaboratively view and edit text files, ensuring exclusive access through locking mechanisms.

The application is implemented using Java, TCP sockets, and concurrent programming (multi-threading).

The server manages a directory of text files, and multiple clients can connect simultaneously to:
- view files
- acquire a file for editing
- save their changes

At any given time, only one client can edit a file.

---

## Technologies Used
- Java
- TCP Sockets
- Concurrency (threads)
- File I/O
- Docker (for running the server)

---

## Architecture

### Server
- Manages client connections (multi-threaded)
- Maintains file states (available / being edited)

### Clients
- Send commands to the server
- Listen for updates asynchronously

Communication is done via TCP sockets using a simple text-based protocol.

---

## Features

### Connection
- Client authenticates using a username
- Receives list of files and their status:
  - available
  - being edited (and by whom)

### File Viewing
- Any client can view file content
- If the file is being edited → read-only mode

### File Editing
- A client can acquire a file only if it is available
- Server:
  - marks file as “being edited”
  - notifies all clients

- If already being edited → request is rejected

### Saving Changes
- Client sends updated version
- Server:
  - saves to disk
  - notifies all clients
  - sends updated content

### Cancel Editing
- Client releases the file
- Server notifies all clients

### Server File Management
- Add file → clients notified
- Delete file → clients receive updated list

---

## Usage Example


Client1: EDIT notes.txt
[SERVER] editing allowed
[SERVER] Editing mode enabled for notes.txt

Client2: EDIT notes.txt
[ERROR] file is already being edited by miruna


---

## Communication Protocol

Available commands:
- LIST
- VIEW
- EDIT
- SAVE
- CANCEL

---

## Running the Application

### 1. Start the server

java server.ServerMain


### 2. Start clients (in separate terminals)

java client.ClientMain


---

## Running with Docker


docker build -t text-editor-server .
docker run -p 1234:1234 text-editor-server


---

## Project Structure

text```
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

## Concurrency and Synchronization

- Multi-threaded server
- Exclusive access for editing
- Automatic release if a client disconnects

---

## Tested Scenarios

- Multiple client connections
- Simultaneous file viewing
- Exclusive editing (single client)
- Rejection of concurrent edit attempts
- Saving and update propagation
- File release after editing
- Forced disconnection and automatic release

---

## Limitations

- No graphical user interface (CLI only)
- No advanced versioning system
- Full file content is transmitted (no diff-based updates)

---

## Possible Improvements

- Add a graphical interface (JavaFX / Web)
- Implement diff-based updates
- Add file versioning
- Introduce user authentication
- Implement conflict resolution strategies

---

## Concepts Used

- Concurrent programming
- Client-server communication
- Synchronization and consistency
- File handling

---

## Team

- Ana-Miruna Grigore
- Mara-Catinca Marinescu
- Marica Maria Daria

**Supervisor:** Ilie-Nemedi Iulian

---

## Notes

This project was developed as part of a university course.

It demonstrates concepts such as concurrent programming, socket-based communication, synchronization, and maintaining consistency across multiple clients.

The project is intended for educational purposes and is part of a professional portfolio.
