package main;

import io.GestorFicheiros;
import modelo.Enfermaria;
import modelo.Episodio;
import modelo.Hospital;
import utils.AnalisadorEstatistico;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal da aplicação.
 */
public class MainHospital {

    /** Separador visual. */
    private static final String SEPARADOR = "=".repeat(60);

    /** Identificador da enfermaria usada no exemplo. */
    private static final String ID_ENFERMARIA_EXEMPLO = "G1";

    /**
     * Executa a demonstração principal.
     *
     * @param args argumentos da linha de comandos
     * @throws IOException se ocorrer erro no acesso aos ficheiros
     */
    public static void main(String[] args) throws IOException {
        Scanner leitor = new Scanner(System.in);

        System.out.println(SEPARADOR);
        System.out.println("  Hospital XYZ - Sistema de Monitorizacao de Camas");
        System.out.println(SEPARADOR);
        System.out.println();
        System.out.println("Menu de configuracao inicial");
        System.out.println(SEPARADOR);
