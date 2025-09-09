/**
 * Trabalho Prático II - Sistemas Operacionais (IEC584) - UFAM
 * Classe Principal: BarbeariaRecrutaZero.java
 *
 * Função:
 * Controla a execução interativa da simulação da Barbearia do Recruta Zero.
 * Permite ao usuário escolher entre os Casos A, B e C, definindo intervalo de cochilo e número de clientes.
 */

import java.util.Scanner;

public class BarbeariaRecrutaZero {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n======================================");
        System.out.println("💈  BARBEARIA DO RECRUTA ZERO  💈");
        System.out.println("======================================\n");

        System.out.println("Escolha o caso para simulação:");
        System.out.println("1 - Caso A: 1 barbeiro com prioridade (Of > Sg > Cb)");
        System.out.println("2 - Caso B: 2 barbeiros com prioridade compartilhada");
        System.out.println("3 - Caso C: 3 barbeiros com cooperação entre filas");
        System.out.print("\nDigite a opção desejada (1 a 3): ");

        int escolha = scanner.nextInt();

        int cochiloMin = 0, cochiloMax = 0;
        while (true) {
            System.out.print("\n⏱️  Digite o tempo MÍNIMO de cochilo do Tainha (1 a 5 segundos): ");
            cochiloMin = scanner.nextInt();
            System.out.print("⏱️  Digite o tempo MÁXIMO de cochilo do Tainha (1 a 5 segundos): ");
            cochiloMax = scanner.nextInt();

            if (cochiloMin >= 1 && cochiloMax <= 5 && cochiloMin <= cochiloMax) break;
            System.out.println("⚠️  Intervalo inválido. Digite valores entre 1 e 5 e com mínimo <= máximo.");
        }

        System.out.print("\n👥 Digite a quantidade de clientes desejada para a simulação: ");
        int totalClientes = scanner.nextInt();

        System.out.println("\n🚀 Iniciando simulação...\n");

        switch (escolha) {
            case 1 -> BarbeariaRecrutaZeroCasoA.executar(totalClientes, cochiloMin, cochiloMax);
            case 2 -> BarbeariaRecrutaZeroCasoB.executar(cochiloMin, cochiloMax, totalClientes);
            case 3 -> BarbeariaRecrutaZeroCasoC.executar(cochiloMin, cochiloMax, totalClientes);
            default -> System.out.println("❌ Opção inválida. Encerrando...");
        }

        scanner.close();
        System.out.println("\n✅ Simulação encerrada com sucesso.");
    }
}
