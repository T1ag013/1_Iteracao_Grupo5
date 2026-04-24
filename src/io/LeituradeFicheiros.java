package io;

import modelo.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Scanner;

public class LeitordeFicheiros {

    private static final int CAPACIDADE_MINIMA = 1;
    private static final String FICHEIRO_LOG = "erros_validacao.log";

    private static void logErro(String msg) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(FICHEIRO_LOG, true));
        out.println("[ERRO] " + LocalDate.now() + ": " + msg);
        out.close();
    }
}