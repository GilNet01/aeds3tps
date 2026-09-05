package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Classe principal: mostra o menu e chama as operações do sistema
public class Main {

    private static final String CAMINHO_BANCO = "pokemon.db";
    private static final String CAMINHO_CSV_PADRAO = "dados/PokemonData.csv";

    private static Scanner scanner = new Scanner(System.in, "UTF-8");
    private static ArquivoSequencial arquivo;

    public static void main(String[] args) throws IOException {
        arquivo = new ArquivoSequencial(CAMINHO_BANCO);

        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ");

            // chama a função certa de acordo com a opção escolhida
            switch (opcao) {
                case 1:
                    opcaoCarregarCSV();
                    break;
                case 2:
                    opcaoInserirManual();
                    break;
                case 3:
                    opcaoLerPorId();
                    break;
                case 4:
                    opcaoAtualizar();
                    break;
                case 5:
                    opcaoDeletar();
                    break;
                case 6:
                    opcaoListarTodos();
                    break;
                case 7:
                    opcaoOrdenacaoExterna();
                    break;
                case 0:
                    System.out.println("Encerrando o sistema...");
                    break;
                default:
                    System.out.println("Opção inválida! Tente novamente.");
                    break;
            }
            System.out.println();
        } while (opcao != 0);

        scanner.close();
    }

    // Mostra as opções do menu na tela
    private static void exibirMenu() {
        System.out.println("===== SISTEMA POKEMON - CRUD SEQUENCIAL =====");
        System.out.println("1. Carregar base de dados (CSV -> binário)");
        System.out.println("2. Inserir novo Pokemon manualmente");
        System.out.println("3. Ler um registro (por ID)");
        System.out.println("4. Atualizar um registro");
        System.out.println("5. Deletar um registro");
        System.out.println("6. Listar todos os registros");
        System.out.println("7. Ordenação Externa");
        System.out.println("0. Sair");
    }

    //Opções do menu

    // Pede o caminho do CSV e carrega os dados no arquivo binário
    private static void opcaoCarregarCSV() {
        System.out.print("Caminho do CSV (ENTER para usar '" + CAMINHO_CSV_PADRAO + "'): ");
        String caminho = scanner.nextLine().trim();
        if (caminho.isEmpty()) caminho = CAMINHO_CSV_PADRAO;

        try {
            int total = CargaCSV.carregar(caminho, arquivo);
            System.out.println("Carga concluída! Total de registros carregados: " + total);
        } catch (IOException e) {
            System.out.println("Erro ao carregar o CSV: " + e.getMessage());
        }
    }

    // Pede os dados de um Pokemon novo e insere no arquivo
    private static void opcaoInserirManual() {
        try {
            Pokemon p = lerDadosPokemonDoTerminal();
            int id = arquivo.create(p);
            System.out.println("Pokemon inserido com sucesso! ID gerado: " + id);
        } catch (IOException e) {
            System.out.println("Erro ao inserir: " + e.getMessage());
        }
    }

    // Pede um ID e mostra o Pokemon correspondente, se existir
    private static void opcaoLerPorId() {
        int id = lerInteiro("Digite o ID do Pokemon: ");
        try {
            Pokemon p = arquivo.read(id);
            if (p == null) {
                System.out.println("Nenhum registro encontrado com o ID " + id);
            } else {
                System.out.println(p);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler: " + e.getMessage());
        }
    }

    // Pede um ID, mostra o registro atual e pede os novos dados para atualizar
    private static void opcaoAtualizar() {
        int id = lerInteiro("Digite o ID do Pokemon que deseja atualizar: ");
        try {
            Pokemon existente = arquivo.read(id);
            if (existente == null) {
                System.out.println("Nenhum registro encontrado com o ID " + id);
                return;
            }

            System.out.println("Registro atual: " + existente);
            System.out.println("Digite os novos dados:");
            Pokemon novosDados = lerDadosPokemonDoTerminal();

            boolean ok = arquivo.update(id, novosDados);
            System.out.println(ok ? "Registro atualizado com sucesso!" : "Falha ao atualizar.");
        } catch (IOException e) {
            System.out.println("Erro ao atualizar: " + e.getMessage());
        }
    }

    // Pede um ID e marca o registro correspondente como excluído
    private static void opcaoDeletar() {
        int id = lerInteiro("Digite o ID do Pokemon que deseja deletar: ");
        try {
            boolean ok = arquivo.delete(id);
            System.out.println(ok ? "Registro deletado com sucesso!" : "Nenhum registro encontrado com esse ID.");
        } catch (IOException e) {
            System.out.println("Erro ao deletar: " + e.getMessage());
        }
    }

    // Mostra todos os registros válidos do arquivo
    private static void opcaoListarTodos() {
        try {
            List<Pokemon> todos = arquivo.listarTodos();
            System.out.println("Total de registros válidos: " + todos.size());
            for (Pokemon p : todos) {
                System.out.println(p);
            }
        } catch (IOException e) {
            System.out.println("Erro ao listar: " + e.getMessage());
        }
    }

    // Pede os parâmetros e chama a ordenação externa do arquivo
    private static void opcaoOrdenacaoExterna() {
        int numCaminhos = lerInteiro("Número de caminhos: ");
        int maxRegistrosMemoria = lerInteiro("Número máximo de registros em memória primária: ");

        try {
            OrdenacaoExterna ordenacao = new OrdenacaoExterna(numCaminhos, maxRegistrosMemoria);
            int total = ordenacao.ordenar(arquivo);
            System.out.println("Ordenação externa concluída! Total de registros no arquivo ordenado: " + total);
            System.out.println("As próximas operações de CRUD serão realizadas sobre o arquivo já ordenado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Parâmetros inválidos: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Erro durante a ordenação externa: " + e.getMessage());
        }
    }

    //leitura

    // Lê todos os campos para montar um Pokemon
    private static Pokemon lerDadosPokemonDoTerminal() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();

        int geracao = lerInteiro("Geração (1 a 8): ");

        System.out.print("Tipos (separados por vírgula, ex: Fire,Flying): ");
        String tiposStr = scanner.nextLine().trim();
        List<String> tipos = new ArrayList<>();
        for (String t : tiposStr.split(",")) {
            t = t.trim();
            if (!t.isEmpty()) tipos.add(t);
        }

        int hp = lerInteiro("HP: ");
        int attack = lerInteiro("Attack: ");
        int defense = lerInteiro("Defense: ");
        int spAtk = lerInteiro("Sp. Atk: ");
        int spDef = lerInteiro("Sp. Def: ");
        int speed = lerInteiro("Speed: ");

        System.out.print("É lendário? (s/n): ");
        boolean legendary = scanner.nextLine().trim().equalsIgnoreCase("s");

        return new Pokemon(0, nome, geracao, tipos, hp, attack, defense, spAtk, spDef, speed, legendary);
    }

    // Lê um numero inteiro, repetindo a pergunta se a entrada for inválida
    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida! Digite um número inteiro.");
            }
        }
    }
}