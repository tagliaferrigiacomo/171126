package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class ArchivioServer {
    private static final int PORTA = 5000;
    private static final Path CARTELLA =
            Paths.get("archivio").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        Files.createDirectories(CARTELLA);

        try (ServerSocket server = new ServerSocket(PORTA)) {
            System.out.println("Server in ascolto sulla porta " + PORTA);

            while (true) {
                Socket connessione = server.accept();
                new Thread(() -> gestisci(connessione)).start();
            }
        }
    }

    private static void gestisci(Socket connessione) {
        try (
            Socket socket = connessione;
            BufferedReader ingresso = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter uscita = new BufferedWriter(
                    new OutputStreamWriter(
                            socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            String nome = ingresso.readLine();

            if (nome == null || nome.isBlank()) {
                invia(uscita, "ERRORE RICHIESTA_VUOTA\n\n");
                return;
            }

            Path file = CARTELLA.resolve(nome).normalize();

            if (!file.startsWith(CARTELLA)) {
                invia(uscita, "ERRORE NOME_NON_VALIDO\n\n");
                return;
            }

            if (!Files.isRegularFile(file)) {
                invia(uscita, "ERRORE FILE_NON_TROVATO\n\n");
                return;
            }

            String nomeFile = file.getFileName().toString();
            int punto = nomeFile.lastIndexOf('.');
            String estensione = punto >= 0
                    ? nomeFile.substring(punto + 1)
                    : "nessuna";

            String contenuto = Files.readString(file, StandardCharsets.UTF_8);

            invia(uscita,
                    "OK\n" +
                    "EXTENSION " + estensione + "\n" +
                    "\n" +
                    contenuto);

        } catch (IOException e) {
            System.err.println("Errore: " + e.getMessage());
        }
    }

    private static void invia(BufferedWriter uscita, String testo)
            throws IOException {
        uscita.write(testo);
        uscita.flush();
    }
}
