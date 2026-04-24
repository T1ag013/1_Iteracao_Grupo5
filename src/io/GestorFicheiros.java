
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
import java.util.Scanner;

/**
 * Classe responsável pela leitura e validação de dados a partir de ficheiros CSV.
 * Os erros de validação são registados no ficheiro {@code erros_validacao.log}.
 * Todos os erros de I/O são propagados com {@code throws}, sem {@code try/catch}.
 *
 * @author Grupo
 * @version 1.0
 */
public class GestorFicheiros {

    /** Caminho do ficheiro de log de erros de validação. */
    private static final String FICHEIRO_LOG = "erros_validacao.log";

    /** Capacidade mínima válida para uma enfermaria. */
    private static final int CAPACIDADE_MINIMA = 1;

    /**
     * Regista uma mensagem de erro no ficheiro de log.
     * O ficheiro é aberto em modo de acrescento para preservar entradas anteriores.
     *
     * @param mensagem mensagem de erro a registar
     * @throws IOException se ocorrer erro ao escrever no ficheiro de log
     */
    private static void logErro(String mensagem) throws IOException {
        PrintWriter escritor = new PrintWriter(new FileWriter(FICHEIRO_LOG, true));
        escritor.println("[ERRO] " + LocalDate.now() + ": " + mensagem);
        escritor.flush();
        escritor.close();
    }

    /**
     * Valida se uma string não é nula nem composta apenas por espaços.
     *
     * @param valor string a validar
     * @return {@code true} se válida, {@code false} caso contrário
     */
    private static boolean validarString(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Valida se uma string representa um número inteiro válido.
     *
     * @param valor string a validar
     * @return {@code true} se for um inteiro válido, {@code false} caso contrário
     */
    private static boolean validarInteiro(String valor) {
        if (!validarString(valor)) return false;
        boolean valido = true;
        char[] caracteres = valor.trim().toCharArray();
        for (int i = 0; i < caracteres.length; i++) {
            if (!Character.isDigit(caracteres[i]) && caracteres[i] != '-') {
                valido = false;
            }
        }
        return valido;
    }

    /**
     * Valida se uma string representa um número decimal válido.
     *
     * @param valor string a validar
     * @return {@code true} se for um decimal válido, {@code false} caso contrário
     */
    private static boolean validarDecimal(String valor) {
        if (!validarString(valor)) return false;
        int pontos = 0;
        boolean valido = true;
        for (char c : valor.trim().toCharArray()) {
            if (c == '.') {
                pontos++;
            } else if (!Character.isDigit(c) && c != '-') {
                valido = false;
            }
        }
        return valido && pontos <= 1;
    }

    /**
     * Valida se uma string representa uma data no formato AAAA-MM-DD.
     *
     * @param valor string a validar
     * @return {@code true} se for uma data válida, {@code false} caso contrário
     */
    private static boolean validarData(String valor) {
        if (!validarString(valor)) return false;
        String[] partes = valor.trim().split("-");
        if (partes.length != 3) return false;
        if (!validarInteiro(partes[0]) || !validarInteiro(partes[1]) || !validarInteiro(partes[2])) return false;
        int mes = Integer.parseInt(partes[1]);
        int dia = Integer.parseInt(partes[2]);
        return mes >= 1 && mes <= 12 && dia >= 1 && dia <= 31;
    }

    /**
     * Valida se uma capacidade é maior ou igual ao mínimo permitido.
     *
     * @param capacidade valor a validar
     * @return {@code true} se válida, {@code false} caso contrário
     */
    private static boolean validarCapacidade(int capacidade) {
        return capacidade >= CAPACIDADE_MINIMA;
    }

    /**
     * Carrega enfermarias a partir de um ficheiro CSV e adiciona-as ao hospital.
     * <p>
     * Formato esperado (separador {@code ;}):
     * <ul>
     *   <li>GERAL;ID;CAPACIDADE;ACOMPANHANTES[;RECURSO1;RECURSO2;...]</li>
     *   <li>PSIQUIATRICA;ID;CAPACIDADE;HORARIO;NIVEL_SEGURANCA</li>
     *   <li>INTENSIVOS;ID;CAPACIDADE;HORARIO;PRESSAO;PRESSAO_REFERENCIA</li>
     * </ul>
     * A primeira linha é tratada como cabeçalho e ignorada.
     * Entradas inválidas são registadas no ficheiro de log.
     *
     * @param path caminho para o ficheiro CSV das enfermarias
     * @param h    hospital onde as enfermarias serão adicionadas
     * @throws IOException           se ocorrer erro ao escrever no ficheiro de log
     * @throws FileNotFoundException se o ficheiro CSV não for encontrado
     */
    public static void carregarEnfermarias(String path, Hospital h)
            throws IOException, FileNotFoundException {

        File f = new File(path);
        if (!f.exists()) {
            System.out.println("Ficheiro nao encontrado: " + path);
        } else {
            Scanner sc = new Scanner(f);
            if (sc.hasNextLine()) sc.nextLine();

            int linha = 1;
            while (sc.hasNextLine()) {
                linha++;
                String[] d = sc.nextLine().trim().split(";");

                if (d.length < 3) {
                    logErro("Linha " + linha + ": campos insuficientes.");
                } else {
                    String tipo   = d[0].trim().toUpperCase();
                    String id     = d[1].trim();
                    String capStr = d[2].trim();

                    if (!validarString(id)) {
                        logErro("Linha " + linha + ": identificador invalido.");
                    } else if (!validarInteiro(capStr)) {
                        logErro("Linha " + linha + ": capacidade nao e um numero inteiro valido.");
                    } else {
                        int cap = Integer.parseInt(capStr);
                        if (!validarCapacidade(cap)) {
                            logErro("Linha " + linha + ": capacidade invalida (" + cap + ").");
                        } else if (tipo.equals("GERAL")) {
                            processarEnfermariaGeral(d, linha, id, cap, h);
                        } else if (tipo.equals("PSIQUIATRICA")) {
                            processarEnfermariaPsiquiatrica(d, linha, id, cap, h);
                        } else if (tipo.equals("INTENSIVOS")) {
                            processarEnfermariaCuidadosIntensivos(d, linha, id, cap, h);
                        } else {
                            logErro("Linha " + linha + ": tipo desconhecido (" + tipo + ").");
                        }
                    }
                }
            }
            sc.close();
        }
    }

    /**
     * Processa e cria uma {@link EnfermariaGeral} a partir dos campos do CSV.
     *
     * @param d       array de campos lidos do CSV
     * @param linha   número da linha no ficheiro (para log)
     * @param id      identificador da enfermaria
     * @param cap     número total de camas
     * @param h       hospital onde a enfermaria será adicionada
     * @throws IOException se ocorrer erro ao escrever no ficheiro de log
     */
    private static void processarEnfermariaGeral(String[] d, int linha,
                                                 String id, int cap, Hospital h)
            throws IOException {

        if (d.length < 5 || !validarInteiro(d[3].trim())) {
            logErro("Linha " + linha + ": GERAL requer acompanhantes e horario validos.");
        } else {
            int acomp = Integer.parseInt(d[3].trim());
            if (acomp < 0) {
                logErro("Linha " + linha + ": numero de acompanhantes nao pode ser negativo.");
            } else if (!validarString(d[4])) {
                logErro("Linha " + linha + ": horario de visitas em branco.");
            } else {
                EnfermariaGeral eg = new EnfermariaGeral(id, cap, acomp, d[4].trim());
                for (int i = 5; i < d.length; i++) {
                    if (validarString(d[i])) {
                        eg.adicionarRecurso(d[i].trim());
                    }
                }
                h.adicionarEnfermaria(eg);
            }
        }
    }

    /**
     * Processa e cria uma {@link EnfermariaPsiquiatrica} a partir dos campos do CSV.
     *
     * @param d       array de campos lidos do CSV
     * @param linha   número da linha no ficheiro (para log)
     * @param id      identificador da enfermaria
     * @param cap     número total de camas
     * @param h       hospital onde a enfermaria será adicionada
     * @throws IOException se ocorrer erro ao escrever no ficheiro de log
     */
    private static void processarEnfermariaPsiquiatrica(String[] d, int linha,
                                                        String id, int cap, Hospital h)
            throws IOException {

        if (d.length < 5 || !validarString(d[3]) || !validarString(d[4])) {
            logErro("Linha " + linha + ": PSIQUIATRICA requer horario e nivel de seguranca.");
        } else {
            h.adicionarEnfermaria(new EnfermariaPsiquiatrica(id, cap, d[3].trim(), d[4].trim()));
        }
    }

    /**
     * Processa e cria uma {@link EnfermariaCuidadosIntensivos} a partir dos campos do CSV.
     *
     * @param d       array de campos lidos do CSV
     * @param linha   número da linha no ficheiro (para log)
     * @param id      identificador da enfermaria
     * @param cap     número total de camas
     * @param h       hospital onde a enfermaria será adicionada
     * @throws IOException se ocorrer erro ao escrever no ficheiro de log
     */
    private static void processarEnfermariaCuidadosIntensivos(String[] d, int linha,
                                                              String id, int cap, Hospital h)
            throws IOException {

        if (d.length < 6 || !validarString(d[3]) || !validarDecimal(d[4]) || !validarDecimal(d[5])) {
            logErro("Linha " + linha + ": INTENSIVOS requer horario, pressao e pressao de referencia validos.");
        } else {
            double pressao    = Double.parseDouble(d[4].trim());
            double pressaoRef = Double.parseDouble(d[5].trim());
            if (pressao <= 0 || pressaoRef <= 0) {
                logErro("Linha " + linha + ": pressoes devem ser positivas.");
            } else {
                h.adicionarEnfermaria(new EnfermariaCuidadosIntensivos(id, cap, d[3].trim(), pressao, pressaoRef));
            }
        }
    }

    /**
     * Carrega episódios a partir de um ficheiro CSV e associa-os às enfermarias do hospital.
     * <p>
     * Formato esperado (separador {@code ;}):
     * <pre>ID_ENFERMARIA;ID_CAMA;DATA_ADMISSAO[;DATA_ALTA]</pre>
     * Datas no formato AAAA-MM-DD. A coluna DATA_ALTA é opcional.
     * A primeira linha é tratada como cabeçalho e ignorada.
     * Entradas inválidas são registadas no ficheiro de log.
     *
     * @param path caminho para o ficheiro CSV dos episódios
     * @param h    hospital com as enfermarias já carregadas
     * @throws IOException           se ocorrer erro ao escrever no ficheiro de log
     * @throws FileNotFoundException se o ficheiro CSV não for encontrado
     */
    public static void carregarEpisodios(String path, Hospital h)
            throws IOException, FileNotFoundException {

        File f = new File(path);
        if (!f.exists()) {
            System.out.println("Ficheiro nao encontrado: " + path);
        } else {
            Scanner sc = new Scanner(f);
            if (sc.hasNextLine()) sc.nextLine();

            int linha = 1;
            while (sc.hasNextLine()) {
                linha++;
                String[] d = sc.nextLine().trim().split(";");

                if (d.length < 3) {
                    logErro("Linha " + linha + ": campos insuficientes no episodio.");
                } else {
                    String idEnfermaria    = d[0].trim();
                    String idCama          = d[1].trim();
                    String dataAdmissaoStr = d[2].trim();

                    if (!validarString(idEnfermaria)) {
                        logErro("Linha " + linha + ": ID de enfermaria invalido.");
                    } else if (!validarString(idCama)) {
                        logErro("Linha " + linha + ": ID de cama invalido.");
                    } else if (!validarData(dataAdmissaoStr)) {
                        logErro("Linha " + linha + ": data de admissao invalida.");
                    } else {
                        Enfermaria enf = h.obterEnfermaria(idEnfermaria);
                        if (enf == null) {
                            logErro("Linha " + linha + ": enfermaria nao encontrada (" + idEnfermaria + ").");
                        } else {
                            LocalDate admissao = LocalDate.parse(dataAdmissaoStr);
                            Episodio ep = new Episodio(idCama, admissao);

                            if (d.length >= 4 && validarString(d[3])) {
                                if (!validarData(d[3].trim())) {
                                    logErro("Linha " + linha + ": data de alta invalida.");
                                } else {
                                    LocalDate alta = LocalDate.parse(d[3].trim());
                                    if (!alta.isAfter(admissao)) {
                                        logErro("Linha " + linha + ": data de alta nao pode ser anterior ou igual a admissao.");
                                    } else {
                                        ep.darAlta(alta);
                                        enf.adicionarEpisodio(ep);
                                    }
                                }
                            } else {
                                enf.adicionarEpisodio(ep);
                            }
                        }
                    }
                }
            }
            sc.close();
        }
    }
}