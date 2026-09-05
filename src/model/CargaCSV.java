package model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CargaCSV {

    public static int carregar(String caminhoCSV, ArquivoSequencial arquivo) throws IOException {
        int totalCarregados = 0;

        //lidar corretamente com acentos
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(caminhoCSV), StandardCharsets.UTF_8))) {

            String linha = br.readLine(); //cabeçalho é descartado

            // Remove caractere invisível
            if (linha != null && linha.startsWith("\uFEFF")) {
                linha = linha.substring(1);
            }

            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) continue;

                String[] campos = linha.split(",", -1);

                if (campos.length < 12) {
                    System.out.println("Linha ignorada (formato inesperado): " + linha);
                    continue;
                }

                try {
                    //colunas: 0=Num 1=Name 2=Type1 3=Type2 4=HP 5=Attack
                    //6=Defense 7=SpAtk 8=SpDef 9=Speed 10=Generation 11=Legendary
                    String name = campos[1].trim();
                    String type1 = campos[2].trim();
                    String type2 = campos[3].trim();
                    int hp = Integer.parseInt(campos[4].trim());
                    int attack = Integer.parseInt(campos[5].trim());
                    int defense = Integer.parseInt(campos[6].trim());
                    int spAtk = Integer.parseInt(campos[7].trim());
                    int spDef = Integer.parseInt(campos[8].trim());
                    int speed = Integer.parseInt(campos[9].trim());
                    int generation = Integer.parseInt(campos[10].trim());
                    boolean legendary = Boolean.parseBoolean(campos[11].trim());

                    List<String> types = new ArrayList<>();
                    types.add(type1);
                    if (!type2.isEmpty()) {
                        types.add(type2);
                    }

                    // id = 0 pq quem gera o id de verdade é o create()
                    Pokemon pokemon = new Pokemon(0, name, generation, types,
                            hp, attack, defense, spAtk, spDef, speed, legendary);

                    arquivo.create(pokemon);
                    totalCarregados++;

                } catch (IllegalArgumentException e) {
                    System.out.println("Linha ignorada (erro ao converter dados): " + linha
                            + " | Motivo: " + e.getMessage());
                }
            }
        }

        return totalCarregados;
    }

    public static void main(String[] args) throws IOException {
        String caminhoBanco = "pokemon.db";
        String caminhoCSV = "dados/PokemonData.csv";

        new java.io.File(caminhoBanco).delete();

        ArquivoSequencial arquivo = new ArquivoSequencial(caminhoBanco);
        int total = CargaCSV.carregar(caminhoCSV, arquivo);

        System.out.println("Total de Pokemon carregados: " + total);

    }
}