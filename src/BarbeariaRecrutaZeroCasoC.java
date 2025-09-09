/**
 * Trabalho Prático II - Sistemas Operacionais (IEC584) - UFAM
 * Tema: Barbearia do Recruta Zero (CASO C)
 * Autora: Beatriz Christine
 *
 * Descrição:
 * Simula uma barbearia com três barbeiros (Recruta Zero, Dentinho e Otto), cada um com uma fila prioritária (oficiais, sargentos, cabos).
 * Os barbeiros cooperam entre si quando sua fila prioritária estiver vazia, respeitando a prioridade geral.
 * O código foi embelezado, os relatórios foram aprimorados e a lógica foi mantida completa e fiel ao enunciado.
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class BarbeariaRecrutaZeroCasoC {

    private static final int TOTAL_CADEIRAS = 20;
    private static final int TENTATIVAS_VAZIAS = 3;
    private static long inicioSimulacao;
    private static int COCHILO_MIN;
    private static int COCHILO_MAX;

    private enum Categoria {
        PAUSA(0), OFICIAL(1), SARGENTO(2), CABO(3);
        final int id;
        Categoria(int id) { this.id = id; }
        public static Categoria fromId(int id) {
            return switch (id) {
                case 1 -> OFICIAL;
                case 2 -> SARGENTO;
                case 3 -> CABO;
                default -> PAUSA;
            };
        }
    }

    private static final BlockingQueue<Cliente> filaOficiais = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Cliente> filaSargentos = new LinkedBlockingQueue<>();
    private static final BlockingQueue<Cliente> filaCabos = new LinkedBlockingQueue<>();
    private static final Semaphore cadeirasDisponiveis = new Semaphore(TOTAL_CADEIRAS);

    private static final AtomicInteger[] totalPorCategoria = new AtomicInteger[4];
    private static final AtomicInteger[] atendidosPorCategoria = new AtomicInteger[4];
    private static final AtomicLong[] tempoTotalAtendimento = new AtomicLong[4];
    private static final AtomicLong[] tempoEsperaTotal = new AtomicLong[4];

 // Acumuladores para ocupação média das filas
    private static long somaOficiaisFila    = 0;
    private static long somaSargentosFila   = 0;
    private static long somaCabosFila       = 0;
    private static long numeroMedicoes      = 0;

    private static class Cliente {
        Categoria categoria;
        long entrada;
        int tempoServico;

        Cliente(Categoria categoria, int tempoServico) {
            this.categoria = categoria;
            this.tempoServico = tempoServico;
            this.entrada = System.currentTimeMillis();
        }
    }

    private static class Tainha extends Thread {
        private final List<String> filaEntrada;
        private int index = 0, tentativasVazias = 0;

        Tainha(List<String> filaEntrada) {
            this.filaEntrada = filaEntrada;
        }

        public void run() {
            System.out.println("\n🎖️ Sargento Tainha iniciou a geração de clientes...");
            while (index < filaEntrada.size() && tentativasVazias < TENTATIVAS_VAZIAS) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(COCHILO_MIN, COCHILO_MAX + 1) * 1000L);
                } catch (InterruptedException ignored) {}

                String linha = filaEntrada.get(index++);
                int categoriaId = Character.getNumericValue(linha.charAt(0));
                Categoria cat = Categoria.fromId(categoriaId);

                if (cat == Categoria.PAUSA) {
                    totalPorCategoria[0].incrementAndGet();
                    tentativasVazias++;
                    System.out.printf("[Tainha] Pausa detectada. Tentativas vazias: %d/%d\n", tentativasVazias, TENTATIVAS_VAZIAS);
                    continue;
                }

                int tempoServico = Character.getNumericValue(linha.charAt(1));

                if (cadeirasDisponiveis.tryAcquire()) {
                    Cliente cliente = new Cliente(cat, tempoServico);
                    totalPorCategoria[cat.id].incrementAndGet();

                    switch (cat) {
                        case OFICIAL -> filaOficiais.add(cliente);
                        case SARGENTO -> filaSargentos.add(cliente);
                        case CABO -> filaCabos.add(cliente);
                    }
                    tentativasVazias = 0;

                    System.out.printf("[Tainha] Cliente %s inserido (serviço: %ds) [%d/%d]\n",
                            cat.name(), tempoServico, index, filaEntrada.size());
                } else {
                    tentativasVazias++;
                    System.out.println("[Tainha] Barbearia cheia. Tentando novamente...\n");
                }
            }
            System.out.printf("\n🎖️ Tainha finalizou. Total gerado: %d | Pausas: %d\n",
                    index, totalPorCategoria[0].get());
        }
    }

    private static class Barbeiro extends Thread {
        private final String nome;
        private final Categoria preferida;

        Barbeiro(String nome, Categoria preferida) {
            this.nome = nome;
            this.preferida = preferida;
        }

        public void run() {
            System.out.printf("\n💈 %s iniciou. Preferência: %s\n", nome, preferida.name());
            while (true) {
                Cliente cliente = null;
                Categoria cat = null;

                try {
                    cliente = takeIfAvailable(preferida);
                    cat = preferida;

                    if (cliente == null) {
                        for (Categoria prioridade : List.of(Categoria.OFICIAL, Categoria.SARGENTO, Categoria.CABO)) {
                            if (prioridade != preferida) {
                                cliente = takeIfAvailable(prioridade);
                                if (cliente != null) {
                                    cat = prioridade;
                                    break;
                                }
                            }
                        }
                    }

                    if (cliente == null) {
                        Thread.sleep(500);
                        continue;
                    }

                    long espera = System.currentTimeMillis() - cliente.entrada;
                    tempoEsperaTotal[cat.id].addAndGet(espera);

                    System.out.printf("💈 %s atendendo %s (serviço: %ds)\n", nome, cat.name(), cliente.tempoServico);

                    Thread.sleep(cliente.tempoServico * 1000L);

                    atendidosPorCategoria[cat.id].incrementAndGet();
                    tempoTotalAtendimento[cat.id].addAndGet(cliente.tempoServico * 1000L);
                    cadeirasDisponiveis.release();

                    System.out.printf("✅ %s finalizou atendimento de %s.\n", nome, cat.name());

                } catch (InterruptedException e) {
                    System.out.printf("✂️ %s encerrou o turno.\n", nome);
                    return;
                }
            }
        }

        private Cliente takeIfAvailable(Categoria cat) {
            return switch (cat) {
                case OFICIAL -> filaOficiais.poll();
                case SARGENTO -> filaSargentos.poll();
                case CABO -> filaCabos.poll();
                default -> null;
            };
        }
    }

    private static class Tenente extends Thread {
        public void run() {
            System.out.println("\n📋 Tenente Escovinha iniciou a supervisão.");
            try {
                while (true) {
                    Thread.sleep(3000);
                    relatorioParcial();
                }
            } catch (InterruptedException e) {
                relatorioParcial();
                relatorioFinal();
            }
        }

        private void relatorioParcial() {
            int ocupadas = TOTAL_CADEIRAS - cadeirasDisponiveis.availablePermits();
            int of = filaOficiais.size(), sa = filaSargentos.size(), ca = filaCabos.size();

            System.out.println("\n📋 === RELATÓRIO PARCIAL ===");
            System.out.printf("Ocupadas: %d/%d (%.2f%%)\n", ocupadas, TOTAL_CADEIRAS, 100.0 * ocupadas / TOTAL_CADEIRAS);
            System.out.printf("Filas → Oficiais: %d | Sargentos: %d | Cabos: %d\n", of, sa, ca);
            System.out.println("==============================");

            somaOficiaisFila  += of;
            somaSargentosFila += sa;
            somaCabosFila     += ca;
            numeroMedicoes++;
        }

        private void relatorioFinal() {
            long duracao = System.currentTimeMillis() - inicioSimulacao;
            System.out.println("\n📋 === RELATÓRIO FINAL ===");
            System.out.printf("⏱️ Duração total: %.2f segundos\n", duracao / 1000.0);

            for (Categoria cat : List.of(Categoria.OFICIAL, Categoria.SARGENTO, Categoria.CABO)) {
                int id = cat.id;
                int atendidos = atendidosPorCategoria[id].get();
                long tempoAt = tempoTotalAtendimento[id].get();
                long tempoEsp = tempoEsperaTotal[id].get();

                double mediaAt = atendidos > 0 ? tempoAt / 1000.0 / atendidos : 0;
                double mediaEsp = atendidos > 0 ? tempoEsp / 1000.0 / atendidos : 0;

                System.out.printf("%s → Atendidos: %d | Média Atendimento: %.2fs | Média Espera: %.2fs\n",
                        cat.name(), atendidos, mediaAt, mediaEsp);
            }

            System.out.printf("👥 Pausas: %d\n", totalPorCategoria[0].get());

            double comprimentoMedio = numeroMedicoes > 0 ? (somaOficiaisFila + somaSargentosFila + somaCabosFila) / (double) numeroMedicoes : 0;
            System.out.printf("📊 Comprimento médio das filas: %.2f\n", comprimentoMedio);

            System.out.printf("📌 Total gerado por categoria: OFICIAL: %d | SARGENTO: %d | CABO: %d\n",
                    totalPorCategoria[1].get(), totalPorCategoria[2].get(), totalPorCategoria[3].get());

            System.out.println("🪑 Estado de ocupação das FILAS (% da capacidade total de " + TOTAL_CADEIRAS + "):");

            double mediaOf = somaOficiaisFila  / (double) numeroMedicoes;
            double mediaSg = somaSargentosFila / (double) numeroMedicoes;
            double mediaCb = somaCabosFila     / (double) numeroMedicoes;
            System.out.printf("   Total das Filas: %.2f%%%n", (mediaOf + mediaSg + mediaCb) * 100.0 / TOTAL_CADEIRAS);
            System.out.printf("   Fila Oficiais: %.2f%%%n", mediaOf * 100.0 / TOTAL_CADEIRAS);
            System.out.printf("   Fila Sargentos: %.2f%%%n", mediaSg * 100.0 / TOTAL_CADEIRAS);
            System.out.printf("   Fila Cabos:     %.2f%%%n", mediaCb * 100.0 / TOTAL_CADEIRAS);
            System.out.println("==============================");
            System.out.println("💈 Barbearia fechada!");
        }
    }

    public static void executar(int cochiloMin, int cochiloMax, int totalClientes) {
        COCHILO_MIN = cochiloMin;
        COCHILO_MAX = cochiloMax;
        inicioSimulacao = System.currentTimeMillis();

        for (int i = 0; i < 4; i++) {
            totalPorCategoria[i] = new AtomicInteger(0);
            atendidosPorCategoria[i] = new AtomicInteger(0);
            tempoTotalAtendimento[i] = new AtomicLong(0);
            tempoEsperaTotal[i] = new AtomicLong(0);
        }

        List<String> filaEntrada = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < totalClientes; i++) {
            int categoria = rand.nextInt(4);
            int tempoServico = switch (categoria) {
                case 1 -> rand.nextInt(3) + 4;
                case 2 -> rand.nextInt(3) + 2;
                case 3 -> rand.nextInt(3) + 1;
                default -> 0;
            };
            filaEntrada.add("" + categoria + tempoServico);
        }

        Tainha tainha = new Tainha(filaEntrada);
        Barbeiro barbeiro1 = new Barbeiro("Recruta Zero", Categoria.OFICIAL);
        Barbeiro barbeiro2 = new Barbeiro("Dentinho", Categoria.SARGENTO);
        Barbeiro barbeiro3 = new Barbeiro("Otto", Categoria.CABO);
        Tenente tenente = new Tenente();

        tainha.start();
        barbeiro1.start();
        barbeiro2.start();
        barbeiro3.start();
        tenente.start();

        try {
            tainha.join();
            while (!filaOficiais.isEmpty() || !filaSargentos.isEmpty() || !filaCabos.isEmpty() ||
                    cadeirasDisponiveis.availablePermits() < TOTAL_CADEIRAS) {
                Thread.sleep(1000);
            }

            barbeiro1.interrupt();
            barbeiro2.interrupt();
            barbeiro3.interrupt();
            tenente.interrupt();
        } catch (InterruptedException ignored) {}
    }
}
