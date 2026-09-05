package model;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Faz a ordenação externa do arquivo de Pokemon, usando intercalação balanceada
public class OrdenacaoExterna {

    private final int numCaminhos;
    private final int maxRegistrosMemoria;
    private final String pastaTemp;

    public OrdenacaoExterna(int numCaminhos, int maxRegistrosMemoria) {
        if (numCaminhos < 2) {
            throw new IllegalArgumentException("O número de caminhos deve ser no mínimo 2.");
        }
        if (maxRegistrosMemoria < 1) {
            throw new IllegalArgumentException("O número máximo de registros em memória deve ser no mínimo 1.");
        }
        this.numCaminhos = numCaminhos;
        this.maxRegistrosMemoria = maxRegistrosMemoria;
        this.pastaTemp = "temp_ordenacao";
    }

    // Faz todo o processo de ordenação e devolve o total de registros ordenados
    public int ordenar(ArquivoSequencial arquivoOriginal) throws IOException {
        java.io.File pasta = criarPastaTemp();

        //Lê todos os registros válidos do arquivo original e os distribui,
        //já ordenados em blocos
        String[] caminhosAtuais = nomesCaminhos("dist");
        int quantidadeRuns = distribuir(arquivoOriginal, caminhosAtuais);
        //se coube tudo em um único bloco/run, já está ordenado.

        //Intercala repetidamente até sobrar apenas 1 run.
        int rodada = 0;
        while (quantidadeRuns > 1) {
            String[] caminhosSaida = nomesCaminhos("merge" + rodada);
            quantidadeRuns = intercalarRodada(caminhosAtuais, caminhosSaida, quantidadeRuns);
            caminhosAtuais = caminhosSaida;
            rodada++;
        }

        //Encontra qual dos caminhos finais efetivamente contém o único run restante
        String caminhoFinal = caminhosAtuais[0];
        for (String c : caminhosAtuais) {
            if (new java.io.File(c).length() > 0) {
                caminhoFinal = c;
                break;
            }
        }
        int total = regravarNoArquivoFinal(caminhoFinal, arquivoOriginal);

        limparPastaTemp(pasta);
        return total;
    }

    // Lê os registros válidos e separa em blocos ordenados, um por caminho
    private int distribuir(ArquivoSequencial arquivoOriginal, String[] caminhos) throws IOException {
        List<Pokemon> todosValidos = arquivoOriginal.listarTodos();

        for (String caminho : caminhos) {
            criarArquivoVazio(caminho);
        }

        int totalRuns = 0;
        int indiceCaminho = 0;
        int i = 0;

        // cada bloco tem no máximo "maxRegistrosMemoria" registros
        while (i < todosValidos.size()) {
            int fim = Math.min(i + maxRegistrosMemoria, todosValidos.size());
            List<Pokemon> bloco = new ArrayList<>(todosValidos.subList(i, fim));

            bloco.sort(Comparator.comparingInt(Pokemon::getId));

            escreverRun(caminhos[indiceCaminho], bloco);

            indiceCaminho = (indiceCaminho + 1) % numCaminhos;
            totalRuns++;
            i = fim;
        }

        return totalRuns;
    }

    // Junta os blocos dos caminhos de entrada em blocos maiores nos caminhos de saída
    private int intercalarRodada(String[] caminhosEntrada, String[] caminhosSaida, int quantidadeRunsAtual)
            throws IOException {

        LeitorDeRuns[] leitores = new LeitorDeRuns[numCaminhos];
        for (int c = 0; c < numCaminhos; c++) {
            leitores[c] = new LeitorDeRuns(caminhosEntrada[c]);
        }

        for (String caminho : caminhosSaida) {
            criarArquivoVazio(caminho);
        }

        int indiceSaida = 0;
        int runsGerados = 0;

        //Enquanto ainda houver pelo menos um run para intercalar nos caminhos de entrada
        while (existeRunPendente(leitores)) {
            //Pega ate 1 run de cada caminho de entrada
            List<List<Pokemon>> gruposParaIntercalar = new ArrayList<>();
            for (LeitorDeRuns leitor : leitores) {
                List<Pokemon> run = leitor.proximoRun();
                if (run != null) {
                    gruposParaIntercalar.add(run);
                }
            }

            List<Pokemon> runIntercalado = intercalarGrupos(gruposParaIntercalar);

            escreverRun(caminhosSaida[indiceSaida], runIntercalado);
            indiceSaida = (indiceSaida + 1) % numCaminhos;
            runsGerados++;
        }

        for (LeitorDeRuns leitor : leitores) {
            leitor.fechar();
        }

        return runsGerados;
    }

    // Verifica se ainda existe algum bloco (run) para ler nos leitores
    private boolean existeRunPendente(LeitorDeRuns[] leitores) throws IOException {
    for (LeitorDeRuns leitor : leitores) {
        if (leitor.temMaisRuns()) return true;
    }
    return false;
}

    // Junta vários blocos ordenados em um só, mantendo a ordem por ID
    private List<Pokemon> intercalarGrupos(List<List<Pokemon>> grupos) {
        int[] indices = new int[grupos.size()];
        List<Pokemon> resultado = new ArrayList<>();

        int totalRestante = 0;
        for (List<Pokemon> g : grupos) totalRestante += g.size();

        // sempre pega o menor ID disponível entre os grupos
        while (totalRestante > 0) {
            int melhorGrupo = -1;
            int menorId = Integer.MAX_VALUE;

            for (int g = 0; g < grupos.size(); g++) {
                if (indices[g] < grupos.get(g).size()) {
                    int idAtual = grupos.get(g).get(indices[g]).getId();
                    if (idAtual < menorId) {
                        menorId = idAtual;
                        melhorGrupo = g;
                    }
                }
            }

            resultado.add(grupos.get(melhorGrupo).get(indices[melhorGrupo]));
            indices[melhorGrupo]++;
            totalRestante--;
        }

        return resultado;
    }

    // Lê o bloco final já ordenado e regrava tudo no arquivo original
    private int regravarNoArquivoFinal(String caminhoOrdenado, ArquivoSequencial arquivoOriginal) throws IOException {
        List<Pokemon> ordenados = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(caminhoOrdenado, "r")) {
            if (raf.length() > 0) {
                int quantidadeRegistros = raf.readInt();
                for (int i = 0; i < quantidadeRegistros; i++) {
                    int tamanho = raf.readInt();
                    byte[] dados = new byte[tamanho];
                    raf.readFully(dados);
                    ordenados.add(Pokemon.fromByteArray(dados));
                }
            }
        }

        //Recria o arquivo original do zero
        String caminhoOriginal = arquivoOriginal.getCaminhoArquivo();
        new java.io.File(caminhoOriginal).delete();

        int maiorId = 0;
        try (RandomAccessFile raf = new RandomAccessFile(caminhoOriginal, "rw")) {
            raf.seek(0);
            raf.writeInt(0);

            // escreve os registros já ordenados e guarda o maior ID
            for (Pokemon p : ordenados) {
                byte[] dadosPokemon = p.toByteArray();
                raf.writeByte(0); 
                raf.writeInt(dadosPokemon.length);
                raf.write(dadosPokemon);
                if (p.getId() > maiorId) maiorId = p.getId();
            }

            raf.seek(0);
            raf.writeInt(maiorId);
        }

        return ordenados.size();
    }

   
    // Escreve um bloco de registros (run) no arquivo de caminho informado
    private void escreverRun(String caminho, List<Pokemon> registros) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminho, "rw")) {
            raf.seek(raf.length());
            raf.writeInt(registros.size());
            for (Pokemon p : registros) {
                byte[] dados = p.toByteArray();
                raf.writeInt(dados.length);
                raf.write(dados);
            }
        }
    }

    //Cria um arquivo vazio no caminho informado.
    private void criarArquivoVazio(String caminho) throws IOException {
        java.io.File f = new java.io.File(caminho);
        if (f.exists()) f.delete();
        new RandomAccessFile(f, "rw").close();
    }

    // Monta o nome dos arquivos temporários usados em cada etapa
    private String[] nomesCaminhos(String prefixo) {
        String[] caminhos = new String[numCaminhos];
        for (int c = 0; c < numCaminhos; c++) {
            caminhos[c] = pastaTemp + java.io.File.separator + prefixo + "_caminho" + c + ".tmp";
        }
        return caminhos;
    }

    // Cria a pasta temporária usada durante a ordenação
    private java.io.File criarPastaTemp() {
        java.io.File pasta = new java.io.File(pastaTemp);
        if (!pasta.exists()) pasta.mkdirs();
        return pasta;
    }

    // Apaga os arquivos temporários e a pasta usada na ordenação
    private void limparPastaTemp(java.io.File pasta) {
        java.io.File[] arquivos = pasta.listFiles();
        if (arquivos != null) {
            for (java.io.File f : arquivos) f.delete();
        }
        pasta.delete();
    }

    // Classe usada para ler os blocos (runs) de um arquivo temporário
    private class LeitorDeRuns {
        private final RandomAccessFile raf;
        private final long tamanhoArquivo;

        LeitorDeRuns(String caminho) throws IOException {
            this.raf = new RandomAccessFile(caminho, "r");
            this.tamanhoArquivo = raf.length();
        }

        // Diz se ainda tem mais blocos para ler no arquivo
        boolean temMaisRuns() throws IOException {
            return raf.getFilePointer() < tamanhoArquivo;
        }

        // Lê o próximo bloco de registros do arquivo
        List<Pokemon> proximoRun() throws IOException {
            if (!temMaisRuns()) return null;

            int quantidadeRegistros = raf.readInt();
            List<Pokemon> run = new ArrayList<>(quantidadeRegistros);
            for (int i = 0; i < quantidadeRegistros; i++) {
                int tamanho = raf.readInt();
                byte[] dados = new byte[tamanho];
                raf.readFully(dados);
                run.add(Pokemon.fromByteArray(dados));
            }
            return run;
        }

        void fechar() throws IOException {
            raf.close();
        }
    }
}