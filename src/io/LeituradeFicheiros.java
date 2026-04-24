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

private static boolean validarString(String valor) {
    return valor != null && !valor.isBlank();
}

private static boolean validarInteiro(String valor) {
    if (!validarString(valor)) return false;
    String limpa = valor.trim();
    for (int i = 0; i < limpa.length(); i++) {
        char c = limpa.charAt(i);
        if (!Character.isDigit(c) && !(i == 0 && c == '-')) return false;
    }
    return true;
}

private static boolean validarDecimal(String valor) {
    if (!validarString(valor)) return false;
    String limpa = valor.trim();
    int pontos = 0;
    for (int i = 0; i < limpa.length(); i++) {
        char c = limpa.charAt(i);
        if (c == '.') pontos++;
        else if (!Character.isDigit(c) && !(i == 0 && c == '-')) return false;
    }
    return pontos <= 1;
}

private static boolean validarData(String valor) {
    if (!validarString(valor)) return false;
    String[] partes = valor.trim().split("-");
    if (partes.length != 3) return false;
    if (!validarInteiro(partes[0]) || !validarInteiro(partes[1]) || !validarInteiro(partes[2])) return false;

    int mes = Integer.parseInt(partes[1]);
    int dia = Integer.parseInt(partes[2]);
    return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
}b
