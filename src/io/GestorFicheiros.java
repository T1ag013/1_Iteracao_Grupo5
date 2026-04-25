package io;

import modelo.Enfermaria;
import modelo.EnfermariaCuidadosIntensivos;
import modelo.EnfermariaGeral;
import modelo.EnfermariaPsiquiatrica;
import modelo.Episodio;
import modelo.Hospital;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe responsável pela leitura e validação de ficheiros CSV.
 */
public class GestorFicheiros {

    /** Caminho do ficheiro de log. */
    private static final String FICHEIRO_LOG = "erros_validacao.log";

    /** Pasta de dados padrão. */
    private static final String PASTA_DADOS = "data";

    /** Capacidade mínima válida para uma enfermaria. */
    private static final int CAPACIDADE_MINIMA = 1;

    /**
     * Limpa o ficheiro de log.
     *
     * @throws IOException se ocorrer erro no acesso ao ficheiro
     */
    public static void limparLog() throws IOException {
        PrintWriter escritor = new PrintWriter(new FileWriter(FICHEIRO_LOG, false));
        escritor.close();
    }

    /**
     * Regista uma mensagem de erro no ficheiro de log.
     *
     * @param mensagem mensagem de erro
     * @throws IOException se ocorrer erro na escrita
     */
    private static void logErro(String mensagem) throws IOException {
        PrintWriter escritor = new PrintWriter(new FileWriter(FICHEIRO_LOG, true));
        escritor.println("[ERRO] " + LocalDate.now() + ": " + mensagem);
        escritor.close();
    }

    /**
     * Resolve um caminho para ficheiro.
     *
     * @param caminho caminho pedido
     * @return ficheiro resolvido
     */
    private static File resolverFicheiro(String caminho) {
        File direto = new File(caminho);
        if (direto.exists()) {
            return direto;
        }
        return new File(PASTA_DADOS, caminho);
    }

    //VALIDAÇÕES
    private static boolean validarString(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static boolean validarInteiro(String valor) {
        if (!validarString(valor)) {
            return false;
        }
        String limpa = valor.trim();
        for (int i = 0; i < limpa.length(); i++) {
            char c = limpa.charAt(i);
            if (!Character.isDigit(c) && !(i == 0 && c == '-')) {
                return false;
            }
        }
        return true;
    }

    private static boolean validarDecimal(String valor) {
        if (!validarString(valor)) {
            return false;
        }
        String limpa = valor.trim();
        int pontos = 0;
        for (int i = 0; i < limpa.length(); i++) {
            char c = limpa.charAt(i);
            if (c == '.') {
                pontos++;
            } else if (!Character.isDigit(c) && !(i == 0 && c == '-')) {
                return false;
            }
        }
        return pontos <= 1;
    }

    private static boolean validarData(String valor) {
        if (!validarString(valor)) {
            return false;
        }
        String[] partes = valor.trim().split("-");
        if (partes.length != 3) {
            return false;
        }
        if (!validarInteiro(partes[0]) || !validarInteiro(partes[1]) || !validarInteiro(partes[2])) {
            return false;
        }
        int mes = Integer.parseInt(partes[1]);
        int dia = Integer.parseInt(partes[2]);
        return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
    }

    private static boolean validarCapacidade(int capacidade) {
        return capacidade >= CAPACIDADE_MINIMA;
    }

    public static void carregarEnfermarias(String path, Hospital hospital) throws IOException {
        File ficheiro = resolverFicheiro(path);
        if (!ficheiro.exists()) {
            System.out.println("  [AVISO] Ficheiro não encontrado: " + ficheiro.getPath());
            return;
        }

        Scanner sc = new Scanner(ficheiro);
        if (sc.hasNextLine()) {
            sc.nextLine();
        }

        int linha = 1;
        while (sc.hasNextLine()) {
            linha++;
            processarLinhaEnfermaria(sc.nextLine(), linha, hospital);
        }
        sc.close();
    }

    private static void processarLinhaEnfermaria(String linhaCsv, int linha, Hospital hospital) throws IOException {
        String conteudo = linhaCsv.trim();
        if (conteudo.isEmpty()) {
            return;
        }

        String[] dados = conteudo.split(";");
        if (dados.length < 4) {
            logErro("Linha " + linha + ": dados insuficientes na enfermaria.");
            return;
        }

        String tipo = dados[0].trim().toUpperCase();
        String id = dados[1].trim();
        String capacidade = dados[2].trim();

        if (!validarString(id)) {
            logErro("Linha " + linha + ": identificador invalido.");
            return;
        }
        if (hospital.obterEnfermaria(id) != null) {
            logErro("Linha " + linha + ": enfermaria repetida (" + id + ").");
            return;
        }
        if (!validarInteiro(capacidade)) {
            logErro("Linha " + linha + ": capacidade invalida.");
            return;
        }

        int numeroCamas = Integer.parseInt(capacidade);
        if (!validarCapacidade(numeroCamas)) {
            logErro("Linha " + linha + ": capacidade invalida (" + numeroCamas + ").");
            return;
        }

        if (tipo.equals("GERAL")) {
            processarEnfermariaGeral(dados, linha, id, numeroCamas, hospital);
        } else if (tipo.equals("PSIQUIATRICA")) {
            processarEnfermariaPsiquiatrica(dados, linha, id, numeroCamas, hospital);
        } else if (tipo.equals("INTENSIVOS")) {
            processarEnfermariaCuidadosIntensivos(dados, linha, id, numeroCamas, hospital);
        } else {
            logErro("Linha " + linha + ": tipo desconhecido (" + tipo + ").");
        }
    }
