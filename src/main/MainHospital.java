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

        String diretorioCsv = lerDiretorioCsv(leitor);
        LocalDate dataReferencia = lerData(leitor, "Introduza a data de referencia (AAAA-MM-DD): ");
        LocalDate dataInicio = lerData(leitor, "Introduza a data inicial do intervalo (AAAA-MM-DD): ");
        LocalDate dataFim = lerDataFim(leitor, dataInicio);

        Hospital hospital = new Hospital("Hospital Central XYZ");
        GestorFicheiros.limparLog();

        System.out.println("A carregar enfermarias do ficheiro CSV...");
        GestorFicheiros.carregarEnfermarias(construirCaminho(diretorioCsv, "enfermarias.csv"), hospital);
        System.out.println("     Enfermarias carregadas: " + hospital.getEnfermarias().size());

        System.out.println(" A carregar episodios do ficheiro CSV...");
        GestorFicheiros.carregarEpisodios(construirCaminho(diretorioCsv, "episodios.csv"), hospital);
        System.out.println("     Consulte 'erros_validacao.log' para entradas rejeitadas.");

        System.out.println("\n" + SEPARADOR);
        System.out.println("  Estrutura carregada");
        System.out.println(SEPARADOR);
        System.out.println(hospital);
        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.println("  " + enf);
        }

        // RF2 - INDICADORES DE OCUPAÇÃO
        System.out.println("\n" + SEPARADOR);
        System.out.printf(" Indicadores de Ocupacao em %s%n", dataReferencia);
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.printf("%n  Enfermaria : %s%n", enf.getIdentificador());
            System.out.printf("  Ocupacao   : %d / %d camas%n",
                    enf.getOcupacaoAbsoluta(dataReferencia), enf.getNumeroCamas());
            System.out.printf("  Taxa       : %.1f%%%n",
                    enf.getTaxaOcupacao(dataReferencia));
            System.out.printf("  Estado     : %s%n",
                    enf.emPressao(dataReferencia) ? "Em pressao" : "Estado normal");
        }

        System.out.println("\n" + SEPARADOR);
        System.out.println(" Sumario de Length of Stay (LoS) por Enfermaria");
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            AnalisadorEstatistico.SumarioLoS sumario = AnalisadorEstatistico.calcularEstatisticasLoS(enf);
            System.out.printf("  %s: %s%n", enf.getIdentificador(), sumario);
        }

        // RF3 - ANÁLISE DE PRESSÃO POR INTERVALO DE DATAS
        System.out.println("\n" + SEPARADOR);
        System.out.printf("Analise de Pressao [%s a %s]%n", DATA_INICIO, DATA_FIM);
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.printf("%nEnfermaria %s:%n", enf.getIdentificador());
            AnalisadorEstatistico.analisarPressaoPorIntervalo(enf, DATA_INICIO, DATA_FIM);
        }

        System.out.println("\n" + SEPARADOR);
        System.out.printf("Episodios da Enfermaria %s %n", ID_ENFERMARIA_EXEMPLO);
        System.out.println(SEPARADOR);

        Enfermaria enfermariaExemplo = hospital.obterEnfermaria(ID_ENFERMARIA_EXEMPLO);
        if (enfermariaExemplo != null) {
            List<Episodio> episodiosOrdenados = enfermariaExemplo.getEpisodiosOrdenadosPorAdmissao();
            for (Episodio ep : episodiosOrdenados) {
                System.out.println("  " + ep);
            }
        }

        System.out.println("\n" + SEPARADOR);
        System.out.printf(" Enfermarias por Taxa de Ocupacao em %s%n", dataReferencia);
        System.out.println(SEPARADOR);

        List<Enfermaria> enfermariasOrdenadas = hospital.listarEnfermariasOrdenadasPorTaxaOcupacao(dataReferencia);
        for (Enfermaria enf : enfermariasOrdenadas) {
            System.out.printf("  %-6s | Taxa: %5.1f%% | %s%n",
                    enf.getIdentificador(),
                    enf.getTaxaOcupacao(dataReferencia),
                    enf.emPressao(dataReferencia) ? "Em pressao" : "Estado normal");
        }

        System.out.println("\n" + SEPARADOR);
        System.out.println("  Fim.");
        System.out.println(SEPARADOR);

        leitor.close();
    }
