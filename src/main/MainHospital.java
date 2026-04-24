

import io.GestorFicheiros;
import modelo.Enfermaria;
import modelo.Episodio;
import modelo.Hospital;
import utils.AnalisadorEstatistico;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Classe principal da aplicação de monitorização de ocupação de camas por enfermaria.
 * No arranque, carrega automaticamente os dados dos ficheiros CSV e demonstra
 * todas as funcionalidades implementadas na Iteração 1.
 *
 * @author Grupo
 * @version 1.0
 */
public class MainHospital {

    /** Separador visual para organização da saída no terminal. */
    private static final String SEPARADOR = "=".repeat(60);

    /** Data de referência utilizada nos cálculos de ocupação. */
    private static final LocalDate DATA_REFERENCIA = LocalDate.of(2024, 3, 10);

    /** Data de início do intervalo de análise de pressão. */
    private static final LocalDate DATA_INICIO = LocalDate.of(2024, 3, 8);

    /** Data de fim do intervalo de análise de pressão. */
    private static final LocalDate DATA_FIM = LocalDate.of(2024, 3, 12);

    /** Identificador da enfermaria usada nas listagens de episódios. */
    private static final String ID_ENFERMARIA_EXEMPLO = "G1";

    /**
     * Ponto de entrada da aplicação.
     * Carrega os dados dos ficheiros CSV e executa a demonstração de todas as funcionalidades.
     *
     * @param args argumentos da linha de comando (não utilizados)
     * @throws IOException se ocorrer erro ao escrever no ficheiro de log de validação
     */
    public static void main(String[] args) throws IOException {

        System.out.println(SEPARADOR);
        System.out.println("  Hospital XYZ - Sistema de Monitorizacao de Camas");
        System.out.println(SEPARADOR);

        // -------------------------------------------------------
        // RF1 - CARREGAR DADOS A PARTIR DE FICHEIROS CSV
        // -------------------------------------------------------
        Hospital hospital = new Hospital("Hospital Central XYZ");

        System.out.println("\nRF1: A carregar enfermarias do ficheiro CSV...");
        GestorFicheiros.carregarEnfermarias("enfermarias.csv", hospital);
        System.out.println("     Enfermarias carregadas: " + hospital.getEnfermarias().size());

        System.out.println("RF1: A carregar episodios do ficheiro CSV...");
        GestorFicheiros.carregarEpisodios("episodios.csv", hospital);
        System.out.println("     Consulte 'erros_validacao.log' para entradas rejeitadas.");

        System.out.println("\n" + SEPARADOR);
        System.out.println("  Estrutura carregada");
        System.out.println(SEPARADOR);
        System.out.println(hospital);
        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.println("  " + enf);
        }

        // -------------------------------------------------------
        // RF2 - INDICADORES DE OCUPAÇÃO
        // -------------------------------------------------------
        System.out.println("\n" + SEPARADOR);
        System.out.printf("  RF2: Indicadores de Ocupacao em %s%n", DATA_REFERENCIA);
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.printf("%n  Enfermaria : %s%n", enf.getIdentificador());
            System.out.printf("  Ocupacao   : %d / %d camas%n",
                    enf.getOcupacaoAbsoluta(DATA_REFERENCIA), enf.getNumeroCamas());
            System.out.printf("  Taxa       : %.1f%%%n",
                    enf.getTaxaOcupacao(DATA_REFERENCIA));
            System.out.printf("  Estado     : %s%n",
                    enf.emPressao(DATA_REFERENCIA) ? "Em pressao" : "Estado normal");
        }

        System.out.println("\n" + SEPARADOR);
        System.out.println("RF2: Sumario de Length of Stay (LoS) por Enfermaria");
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            AnalisadorEstatistico.SumarioLoS sumario = AnalisadorEstatistico.calcularEstatisticasLoS(enf);
            System.out.printf("  %s: %s%n", enf.getIdentificador(), sumario);
        }

        // -------------------------------------------------------
        // RF3 - ANÁLISE DE PRESSÃO POR INTERVALO DE DATAS
        // -------------------------------------------------------
        System.out.println("\n" + SEPARADOR);
        System.out.printf("Analise de Pressao [%s a %s]%n", DATA_INICIO, DATA_FIM);
        System.out.println(SEPARADOR);

        for (Enfermaria enf : hospital.getEnfermarias()) {
            System.out.printf("%nEnfermaria %s:%n", enf.getIdentificador());
            AnalisadorEstatistico.analisarPressaoPorIntervalo(enf, DATA_INICIO, DATA_FIM);
        }

        // -------------------------------------------------------
        // RF4 - LISTAGENS ORDENADAS
        // -------------------------------------------------------
        System.out.println("\n" + SEPARADOR);
        System.out.printf("Episodios da Enfermaria %s (por data de admissao)%n", ID_ENFERMARIA_EXEMPLO);
        System.out.println(SEPARADOR);

        Enfermaria enfermariaExemplo = hospital.obterEnfermaria(ID_ENFERMARIA_EXEMPLO);
        if (enfermariaExemplo != null) {
            List<Episodio> episodiosOrdenados = enfermariaExemplo.getEpisodiosOrdenadosPorAdmissao();
            for (Episodio ep : episodiosOrdenados) {
                System.out.println("  " + ep);
            }
        }

        System.out.println("\n" + SEPARADOR);
        System.out.printf("  RF4: Enfermarias por Taxa de Ocupacao em %s (decrescente)%n", DATA_REFERENCIA);
        System.out.println(SEPARADOR);

        List<Enfermaria> enfermariasOrdenadas = hospital.getEnfermarias();
        AnalisadorEstatistico.ordenarPorTaxaOcupacao(enfermariasOrdenadas, DATA_REFERENCIA);
        for (Enfermaria enf : enfermariasOrdenadas) {
            System.out.printf("  %-6s | Taxa: %5.1f%% | %s%n",
                    enf.getIdentificador(),
                    enf.getTaxaOcupacao(DATA_REFERENCIA),
                    enf.emPressao(DATA_REFERENCIA) ? "Em pressao" : "Estado normal");
        }

        System.out.println("\n" + SEPARADOR);
        System.out.println("  Fim da execucao.");
        System.out.println(SEPARADOR);
    }
}
