package model;

// IMPORTANTE: este teste precisa estar no pacote "model" (mesma pasta do
// Pokemon.java), porque ArquivoSequencial não é public — só pode ser usada
// por classes do mesmo pacote.

import java.util.Arrays;
import java.util.List;

public class TesteCRUD {
    public static void main(String[] args) throws Exception {
        String caminho = "teste_pokemon.db";
        new java.io.File(caminho).delete(); // garante teste limpo

        ArquivoSequencial arquivo = new ArquivoSequencial(caminho);

        // ---------- CREATE ----------
        int id1 = arquivo.create(new Pokemon(0, "Bulbasaur", 1, Arrays.asList("Grass", "Poison"),
                45, 49, 49, 65, 65, 45, false));
        int id2 = arquivo.create(new Pokemon(0, "Charmander", 1, Arrays.asList("Fire"),
                39, 52, 43, 60, 50, 65, false));
        int id3 = arquivo.create(new Pokemon(0, "Mewtwo", 1, Arrays.asList("Psychic"),
                106, 150, 70, 194, 120, 140, true));

        System.out.println("IDs gerados: " + id1 + ", " + id2 + ", " + id3);
        System.out.println();

        // ---------- READ ----------
        System.out.println("== READ ==");
        System.out.println(arquivo.read(id1));
        System.out.println(arquivo.read(id2));
        System.out.println(arquivo.read(id3));
        System.out.println("Read id inexistente (99): " + arquivo.read(99));
        System.out.println();

        // ---------- UPDATE (mesmo tamanho) ----------
        System.out.println("== UPDATE (mesmo tamanho: 'Charmander' -> 'Charmandar') ==");
        Pokemon atualizadoMesmoTamanho = new Pokemon(0, "Charmandar", 1, Arrays.asList("Fire"),
                39, 52, 43, 60, 50, 65, false);
        boolean ok1 = arquivo.update(id2, atualizadoMesmoTamanho);
        System.out.println("Atualizado? " + ok1);
        System.out.println(arquivo.read(id2));
        System.out.println();

        // ---------- UPDATE (tamanho diferente) ----------
        System.out.println("== UPDATE (tamanho diferente: 'Bulbasaur' -> 'BulbasaurMegaForm') ==");
        Pokemon atualizadoTamanhoDiferente = new Pokemon(0, "BulbasaurMegaForm", 1,
                Arrays.asList("Grass", "Poison"), 80, 82, 83, 100, 100, 80, false);
        boolean ok2 = arquivo.update(id1, atualizadoTamanhoDiferente);
        System.out.println("Atualizado? " + ok2);
        System.out.println(arquivo.read(id1));
        System.out.println();

        // ---------- DELETE ----------
        System.out.println("== DELETE (Mewtwo) ==");
        boolean ok3 = arquivo.delete(id3);
        System.out.println("Excluído? " + ok3);
        System.out.println("Read depois de excluir: " + arquivo.read(id3));
        System.out.println();

        // ---------- LISTAR TODOS (deve mostrar só os válidos) ----------
        System.out.println("== LISTAR TODOS (válidos) ==");
        List<Pokemon> todos = arquivo.listarTodos();
        for (Pokemon p : todos) {
            System.out.println(p);
        }
        System.out.println("Total de registros válidos: " + todos.size() + " (esperado: 2)");
    }
}