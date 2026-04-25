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
    /**
     * Valida uma string.
     *
     * @param valor valor a validar
     * @return {@code true} se o valor for válido
     */
    private static boolean validarString(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Valida um inteiro.
     *
     * @param valor string a validar
     * @return {@code true} se o valor for inteiro
     */
    private static boolean validarInteiro(String valor) {
        if (!validarString(valor)) {
            return false;
        }
        String limpa = valor.trim();
        for (int i = 0; i < limpa.length(); i++) {
            char c = limpa.charAt(i);
            // Se NÃO for um dígito e não for o índice 0 com um sinal de menos, da return em falso
            if (!Character.isDigit(c) && !(i == 0 && c == '-')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida um decimal.
     *
     * @param valor string a validar
     * @return {@code true} se o valor for decimal
     */
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
                // Se detetar um segundo ponto (ex: 12.5.3), morre logo aqui
                if (pontos > 1) {
                    return false;
                }
            } else if (!Character.isDigit(c) && !(i == 0 && c == '-')) {
                return false;
            }
        }
        return false;
    }

    /**
     * Valida uma data no formato YYYY-MM-DD.
     *
     * @param valor string a validar
     * @return {@code true} se a data for válida
     */
    private static boolean validarData(String valor) {
        if (!validarString(valor)) {
            return false;
        }
        // Corta a string pelos traços
        String[] partes = valor.trim().split("-");
        // Se não tiver exatamente 3 pedaços (Ano, Mês, Dia), chumba logo
        if (partes.length != 3) {
            return false;
        }
        // Usa o teu método validarInteiro para garantir que não há letras no meio da data
        if (!validarInteiro(partes[0]) || !validarInteiro(partes[1]) || !validarInteiro(partes[2])) {
            return false;
        }
        int ano = Integer.parseInt(partes[0]);
        int mes = Integer.parseInt(partes[1]);
        int dia = Integer.parseInt(partes[2]);
        // Validações básicas do calendário
        if (ano < 1) {
            return false;
        }
        if (mes < 1 || mes > 12) {
            return false;
        }
        if (dia < 1 || dia > 31) {
            return false;
        }

        // Meses que só têm 30 dias (Abril, Junho, Setembro, Novembro)
        if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) {
            return false;
        }
        return true;
    }

    /**
     * Valida uma capacidade.
     *
     * @param capacidade valor a validar
     * @return {@code true} se a capacidade for válida
     */
    private static boolean validarCapacidade(int capacidade) {
        return capacidade >= CAPACIDADE_MINIMA;
    }

    /**
     * Carrega enfermarias a partir de CSV.
     *
     * @param path caminho para o ficheiro CSV das enfermarias
     * @param hospital hospital onde as enfermarias serão adicionadas
     * @throws IOException se ocorrer erro no acesso ao ficheiro
     */
    public static void carregarEnfermarias(String path, Hospital hospital) throws IOException {
        File ficheiro = resolverFicheiro(path);
        if (!ficheiro.exists()) {
            System.out.println(" Ficheiro não encontrado: " + ficheiro.getPath());
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


    /**
     * Processa e valida uma única linha do ficheiro CSV de enfermarias.
     * Utiliza cláusulas de guarda para rejeitar dados mal formatados
     *
     * @param linhaCsv a linha de texto em bruto lida do ficheiro
     * @param linha    o número da linha atual
     * @param hospital a instância do hospital onde a enfermaria será verificada e adicionada
     * @throws IOException caso ocorra uma falha na escrita do ficheiro de log
     */
    private static void processarLinhaEnfermaria(String linhaCsv, int linha, Hospital hospital) throws IOException {
        String conteudo = linhaCsv.trim();

        if (conteudo.isEmpty()) {
            return;
        }

        String[] dados = conteudo.split(";");

        // Barreira 1: Estrutura mínima. Todas as enfermarias exigem pelo menos Tipo, ID, Capacidade e o outro parametro
        if (dados.length < 4) {
            logErro("Linha " + linha + ": dados insuficientes na enfermaria.");
            return;
        }

        String tipo = dados[0].trim().toUpperCase();
        String id = dados[1].trim();
        String capacidade = dados[2].trim();

        // Barreira 2: Integridade do Identificador
        if (!validarString(id)) {
            logErro("Linha " + linha + ": identificador invalido.");
            return;
        }

        // Barreira 3:Prevenção de Duplicados em Memória
        if (hospital.obterEnfermaria(id) != null) {
            logErro("Linha " + linha + ": enfermaria repetida (" + id + ").");
            return;
        }

        // Barreira 4: Prevenção contra a conversão para Inteiro
        if (!validarInteiro(capacidade)) {
            logErro("Linha " + linha + ": capacidade invalida.");
            return;
        }

        int numeroCamas = Integer.parseInt(capacidade);

        // Barreira 5: Validação do limite mínimo de camas estabelecido
        if (!validarCapacidade(numeroCamas)) {
            logErro("Linha " + linha + ": capacidade invalida (" + numeroCamas + ").");
            return;
        }

        // Dá responsabilidades consoante o tipo específico de enfermaria
        if (tipo.equals("GERAL")) {
            processarEnfermariaGeral(dados, linha, id, numeroCamas, hospital);
        } else if (tipo.equals("PSIQUIATRICA")) {
            processarEnfermariaPsiquiatrica(dados, linha, id, numeroCamas, hospital);
        } else if (tipo.equals("INTENSIVOS")) {
            processarEnfermariaCuidadosIntensivos(dados, linha, id, numeroCamas, hospital);
        } else {
            // Trata tipos de enfermaria não suportados ou erros de digitação no CSV
            logErro("Linha " + linha + ": tipo desconhecido (" + tipo + ").");
        }
    }
}

/**
 * Constrói e regista uma Enfermaria Geral no sistema.

 * @param dados       o array de strings resultante da separação da linha do CSV
 * @param linha       o número da linha atual (utilizado para rastreio no log)
 * @param id          o identificador único da enfermaria (já pré-validado)
 * @param numeroCamas a capacidade máxima da enfermaria (já pré-validada)
 * @param hospital    a instância do hospital onde a enfermaria será injetada
 * @throws IOException caso ocorra falha na escrita do ficheiro de log
 */
private static void processarEnfermariaGeral(String[] dados, int linha, String id,
                                             int numeroCamas, Hospital hospital) throws IOException {

    // Esta barreira valida se na quarta coluna temos um numero (nunmero de acompanhantes), caso contrario temos um erro
    if (dados.length < 5 || !validarInteiro(dados[3].trim()) || !validarString(dados[4])) {
        logErro("Linha " + linha + ": dados invalidos para enfermaria geral.");
        return;
    }

    // Conversão segura
    int acompanhantes = Integer.parseInt(dados[3].trim());

    // Impede que se coloquem um numero de acompanhantes negativo
    if (acompanhantes < 0) {
        logErro("Linha " + linha + ": numero de acompanhantes invalido.");
        return;
    }

    // Instanciação base com os dados obrigatórios
    EnfermariaGeral enfermaria = new EnfermariaGeral(id, numeroCamas, acompanhantes, dados[4].trim());
    for (int i = 5; i < dados.length; i++) {
        if (validarString(dados[i])) {
            enfermaria.adicionarRecurso(dados[i].trim());
        }
    }

    hospital.adicionarEnfermaria(enfermaria);
}

/**
 * Processa uma enfermaria psiquiátrica.
 *
 * @param dados campos do CSV
 * @param linha número da linha
 * @param id identificador da enfermaria
 * @param numeroCamas número de camas
 * @param hospital hospital de destino
 * @throws IOException se ocorrer erro na escrita
 */
private static void processarEnfermariaPsiquiatrica(String[] dados, int linha, String id, int numeroCamas, Hospital hospital) throws IOException {

    // Garante que os atributos do índice 3 e 4 (horário e nível de segurança existem e não são texto em branco.
    if (dados.length < 5 || !validarString(dados[3]) || !validarString(dados[4])) {
        logErro("Linha " + linha + ": dados invalidos para enfermaria psiquiatrica.");
        return;
    }
    hospital.adicionarEnfermaria(new EnfermariaPsiquiatrica(id, numeroCamas, dados[3].trim(), dados[4].trim()));
}


/**
 * Processa uma enfermaria de cuidados intensivos.
 *
 * @param dados campos do CSV
 * @param linha número da linha
 * @param id identificador da enfermaria
 * @param numeroCamas número de camas
 * @param hospital hospital de destino
 * @throws IOException se ocorrer erro na escrita
 */
private static void processarEnfermariaCuidadosIntensivos(String[] dados, int linha, String id, int numeroCamas, Hospital hospital) throws IOException {

    //Garantimos que os Cuidados Intensivos têm 6 colunas no total e também que as colunas das pressões  têm formato decimal
    if (dados.length < 6 || !validarString(dados[3]) || !validarDecimal(dados[4]) || !validarDecimal(dados[5])) {
        logErro("Linha " + linha + ": dados invalidos para cuidados intensivos.");
        return;
    }

    double pressao = Double.parseDouble(dados[4].trim());
    double pressaoReferencia = Double.parseDouble(dados[5].trim());
    //Num ambiente hospitalar, os valores de pressão devem ser positivos.
    if (pressao <= 0 || pressaoReferencia <= 0) {
        logErro("Linha " + linha + ": pressoes invalidas.");
        return;
    }

    hospital.adicionarEnfermaria(new EnfermariaCuidadosIntensivos(id, numeroCamas, dados[3].trim(),pressao, pressaoReferencia));
}
