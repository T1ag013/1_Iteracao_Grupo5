package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 *Representa uma enfermaria geral, com suporte a acompanhantes e lista de recursos disponíveis.
 *Estende {@link Enfermaria} com atributos específicos de uma unidade geral.
 */
public class EnfermariaGeral extends Enfermaria {

  /** Número máximo de acompanhantes permitidos por paciente. */
  private int limiteAcompanhantes;
  
  /** Horário de visitas da enfermaria. */
  private String horarioVisitas;

  /**
   * Cria uma nova enfermaria geral.
   *
   * @param id                  identificador único de enfermaria
   * @param camas               número toal de camas
   * @param limiteAcompanhantes número máximo de acompanhantes por paciente
   * @param horarioVisitas      hor+ário de visitas permitido
   */
  public EnfermariaGeral(String id, int camas, int limiteAcompanhantes, String horarioVisitas) {
    super (id, camas);
    this.limiteAcompanhantes = limiteAcompanhantes;
    this.horarioVisitas = horarioVisitas;
  }
}
