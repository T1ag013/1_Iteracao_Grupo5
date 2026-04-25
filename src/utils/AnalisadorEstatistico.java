package utils;

import modelo.Enfermaria;

import java.time.LocalDate;
import java.util.List;

/**
 * Fornece cálculos estatísticos simples.
 */
public class AnalisadorEstatistico {

    /**
     * Representa um resumo estatístico do LoS.
     */
    public static class SumarioLoS {

        /** Número de episódios considerados. */
        private int totalEpisodios;

        /** Média do LoS. */
        private double media;

        /** Desvio Padrão do LoS. */
        private double desvioPadrao;

        /** Valor mínimo do LoS. */
        private long minimo;

        /** Valor máximo do LoS. */
        private long maximo;

        /**
         * Cria um resumo de LoS.
         *
         * @param totalEpisodios número de episódios
         * @param media média
         * @param desvioPadrao desvio padrão
         * @param minimo mínimo
         * @param maximo máximo
         */
        public SumarioLoS(int totalEpisodios, double media, double desvioPadrao, long minimo, long maximo) {
            this.totalEpisodios = totalEpisodios;
            this.media = media;
            this.desvioPadrao = desvioPadrao;
            this.minimo = minimo;
            this.maximo = maximo;
        }

        /**
         * Devolve o número de epis´dios.
         *
         * @return número de episódios
         */
        public int getTotalEpisodios() {
            return totalEpisodios;
        }

        /**
         * Devolve a média.
         *
         * @return média
         */
        public double getMedia() {
            return media;
        }

        /**
         * Devolve o desvio padrão.
         *
         * @return desvio padrão
         */
        public double getDesvioPadrao() {
            return desvioPadrao;
        }

        /**
         * Devolve o mínimo.
         *
         * @return mínimo
         */
        public long getMinimo() {
            return minimo;
        }

        /**
         * Devolve o máximo.
         *
         * @return máximo
         */
        public long getMaximo() {
            return maximo;
        }

        @Override
        public String toString(){
            if (totalEpisodios == 0){
                return "Sem episódios com alta";
            }
            return String.format("n=%d | media=%.2f | dp=%.2f | min=%d | max=%d",
                    totalEpisodios, media, desvioPadrao, minimo, maximo);
        }
    }
}

