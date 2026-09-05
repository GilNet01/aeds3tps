package model;

import java.util.Arrays;
import java.util.List;

public class TesteOrdenacaoExterna {
    public static void main(String[] args) throws Exception {
        String caminho = "teste_ordenacao.db";
        new java.io.File(caminho).delete();

        ArquivoSequencial arquivo = new ArquivoSequencial(caminho);

        // Insere alguns registros fora de ordem de ID (ids serão 1,2,3,... na ordem de create)
        int idBulba = arquivo.create(new Pokemon(0, "Bulbasaur", 1, Arrays.asList("Grass", "Poison"),
                45, 49, 49, 65, 65, 45, false));
        int idChar = arquivo.create(new Pokemon(0, "Charmander", 1, Arrays.asList("Fire"),
                39, 52, 43, 60, 50, 65, false));
        int idSquirt = arquivo.create(new Pokemon(0, "Squirtle", 1, Arrays.asList("Water"),
                44, 48, 65, 50, 64, 43, false));
        int idMewtwo = arquivo.create(new Pokemon(0, "Mewtwo", 1, Arrays.asList("Psychic"),
                106, 150, 70, 194, 120, 140, true));
        int idPika = arquivo.create(new Pokemon(0, "Pikachu", 1, Arrays.asList("Electric"),
                35, 55, 40, 50, 50, 90, false));

        // Deleta um registro (deve sumir após a ordenação)
        arquivo.delete(idSquirt);

        // Atualiza um registro trocando o tamanho (gera "lixo"/registro excluído no meio do arquivo)
        arquivo.update(idChar, new Pokemon(0, "CharmanderEvoluido", 1, Arrays.asList("Fire"),
                58, 64, 58, 80, 65, 80, false));

        System.out.println("Antes da ordenação:");
        for (Pokemon p : arquivo.listarTodos()) System.out.println(p);
        System.out.println();

        // Executa a ordenação externa com poucos caminhos e pouca memória, para forçar múltiplas rodadas
        OrdenacaoExterna ordenacao = new OrdenacaoExterna(2, 2);
        int total = ordenacao.ordenar(arquivo);

        System.out.println("Total apos ordenacao: " + total + " (esperado: 4)");
        System.out.println();

        List<Pokemon> depois = arquivo.listarTodos();
        System.out.println("Depois da ordenação (deve estar em ordem crescente de ID, sem Squirtle):");
        int idAnterior = -1;
        boolean ordenadoCorretamente = true;
        for (Pokemon p : depois) {
            System.out.println(p);
            if (p.getId() < idAnterior) ordenadoCorretamente = false;
            idAnterior = p.getId();
        }

        System.out.println();
        System.out.println("Ordenado corretamente? " + ordenadoCorretamente);

        // Confirma que CRUD continua funcionando normalmente após a ordenação
        Pokemon lido = arquivo.read(idMewtwo);
        System.out.println("Read pos-ordenacao (Mewtwo): " + lido);

        int novoId = arquivo.create(new Pokemon(0, "Eevee", 1, Arrays.asList("Normal"),
                55, 55, 50, 45, 65, 55, false));
        System.out.println("Novo create pos-ordenacao, id gerado: " + novoId + " (esperado: 6)");
    }
}