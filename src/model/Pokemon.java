package model;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Representa um Pokemon, com seus atributos e conversão para bytes
public class Pokemon {

    public static final int TAM_GENERATION_CODE = 3;

    private int id;
    private String name;
    private String generationCode;
    private LocalDate releaseDate;
    private List<String> types;
    private int hp;
    private int attack;
    private int defense;
    private int spAtk;
    private int spDef;
    private int speed;
    private boolean legendary;

    public Pokemon() {
        this.types = new ArrayList<>();
    }

    public Pokemon(int id, String name, int generation, List<String> types,
                   int hp, int attack, int defense, int spAtk, int spDef, int speed,
                   boolean legendary) {
        this.id = id;
        this.name = name;
        setGeneration(generation);
        this.types = types;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAtk = spAtk;
        this.spDef = spDef;
        this.speed = speed;
        this.legendary = legendary;
    }

    // Retorna a data de lançamento de acordo com a geração do Pokemon
    public static LocalDate dataDaGeracao(int generation) {
        switch (generation) {
            case 1: return LocalDate.of(1996, 2, 27);
            case 2: return LocalDate.of(1999, 11, 21);
            case 3: return LocalDate.of(2002, 11, 21);
            case 4: return LocalDate.of(2006, 9, 28);
            case 5: return LocalDate.of(2010, 9, 18);
            case 6: return LocalDate.of(2013, 10, 12);
            case 7: return LocalDate.of(2016, 11, 18);
            case 8: return LocalDate.of(2019, 11, 15);
            default: throw new IllegalArgumentException("Geração desconhecida: " + generation);
        }
    }

    // Monta o código da geração (ex: "G1") e já define a data de lançamento
    public void setGeneration(int generation) {
        String codigo = "G" + generation;
        while (codigo.length() < TAM_GENERATION_CODE) {
            codigo += " ";
        }
        this.generationCode = codigo;
        this.releaseDate = dataDaGeracao(generation);
    }

    public int getGeneration() {
        return Integer.parseInt(generationCode.trim().substring(1));
    }

    // Transforma o Pokemon em um vetor de bytes para salvar no arquivo
    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(id);

        byte[] nameBytes = name.getBytes("UTF-8");
        dos.writeInt(nameBytes.length);
        dos.write(nameBytes);

        byte[] genBytes = generationCode.getBytes("UTF-8");
        dos.write(genBytes, 0, TAM_GENERATION_CODE);

        int dataAsInt = releaseDate.getYear() * 10000
                + releaseDate.getMonthValue() * 100
                + releaseDate.getDayOfMonth();
        dos.writeInt(dataAsInt);

        String typesStr = String.join(";", types);
        byte[] typesBytes = typesStr.getBytes("UTF-8");
        dos.writeInt(typesBytes.length);
        dos.write(typesBytes);

        dos.writeInt(hp);
        dos.writeInt(attack);
        dos.writeInt(defense);
        dos.writeInt(spAtk);
        dos.writeInt(spDef);
        dos.writeInt(speed);

        dos.writeBoolean(legendary);

        dos.flush();
        return baos.toByteArray();
    }

    // Monta um Pokemon a partir de um vetor de bytes lido do arquivo
    public static Pokemon fromByteArray(byte[] dadosBytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(dadosBytes);
        DataInputStream dis = new DataInputStream(bais);

        Pokemon p = new Pokemon();

        p.id = dis.readInt();

        int nameLen = dis.readInt();
        byte[] nameBytes = new byte[nameLen];
        dis.readFully(nameBytes);
        p.name = new String(nameBytes, "UTF-8");

        byte[] genBytes = new byte[TAM_GENERATION_CODE];
        dis.readFully(genBytes);
        p.generationCode = new String(genBytes, "UTF-8");

        int dataAsInt = dis.readInt();
        int ano = dataAsInt / 10000;
        int mes = (dataAsInt / 100) % 100;
        int dia = dataAsInt % 100;
        p.releaseDate = LocalDate.of(ano, mes, dia);

        int typesLen = dis.readInt();
        byte[] typesBytes = new byte[typesLen];
        dis.readFully(typesBytes);
        String typesStr = new String(typesBytes, "UTF-8");
        p.types = new ArrayList<>();
        for (String t : typesStr.split(";")) {
            if (!t.isEmpty()) p.types.add(t);
        }

        p.hp = dis.readInt();
        p.attack = dis.readInt();
        p.defense = dis.readInt();
        p.spAtk = dis.readInt();
        p.spDef = dis.readInt();
        p.speed = dis.readInt();

        p.legendary = dis.readBoolean();

        return p;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenerationCode() { return generationCode; }

    public LocalDate getReleaseDate() { return releaseDate; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getSpAtk() { return spAtk; }
    public void setSpAtk(int spAtk) { this.spAtk = spAtk; }

    public int getSpDef() { return spDef; }
    public void setSpDef(int spDef) { this.spDef = spDef; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public boolean isLegendary() { return legendary; }
    public void setLegendary(boolean legendary) { this.legendary = legendary; }

    @Override
    public String toString() {
        return String.format(
            "ID: %d | Nome: %s | Geração: %s | Lançamento: %s | Tipos: %s | " +
            "HP: %d Atk: %d Def: %d SpAtk: %d SpDef: %d Speed: %d | Lendário: %s",
            id, name, generationCode.trim(), releaseDate, String.join(";", types),
            hp, attack, defense, spAtk, spDef, speed, legendary ? "Sim" : "Não"
        );
    }
}


// Cuida do arquivo binário: guarda, lê, atualiza e apaga os registros de Pokemon
class ArquivoSequencial {

    private static final int TAM_CABECALHO = 4;
    private static final int TAM_CABECALHO_REGISTRO = 1 + 4;

    private static final byte VALIDO = 0;
    private static final byte EXCLUIDO = 1;

    private final String caminhoArquivo;

    public ArquivoSequencial(String caminhoArquivo) throws IOException {
        this.caminhoArquivo = caminhoArquivo;
        inicializarArquivoSeNecessario();
    }

    // Cria o arquivo com o cabeçalho zerado, se ele ainda não existir
    private void inicializarArquivoSeNecessario() throws IOException {
        File f = new File(caminhoArquivo);
        boolean existiaAntes = f.exists();
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            if (!existiaAntes || raf.length() < TAM_CABECALHO) {
                raf.seek(0);
                raf.writeInt(0);
            }
        }
    }

    // Lê o último ID usado, guardado no cabeçalho do arquivo
    private int lerUltimoId(RandomAccessFile raf) throws IOException {
        raf.seek(0);
        return raf.readInt();
    }

    // Atualiza o último ID usado no cabeçalho do arquivo
    private void escreverUltimoId(RandomAccessFile raf, int ultimoId) throws IOException {
        raf.seek(0);
        raf.writeInt(ultimoId);
    }

    // Gera um novo ID e escreve o Pokemon no final do arquivo
    public int create(Pokemon pokemon) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "rw")) {
            int ultimoId = lerUltimoId(raf);
            int novoId = ultimoId + 1;
            pokemon.setId(novoId);

            escreverRegistroNoFim(raf, pokemon);

            escreverUltimoId(raf, novoId);
            return novoId;
        }
    }

    // Escreve o registro (lápide + tamanho + dados) no final do arquivo
    private void escreverRegistroNoFim(RandomAccessFile raf, Pokemon pokemon) throws IOException {
        byte[] dados = pokemon.toByteArray();

        raf.seek(raf.length());
        raf.writeByte(VALIDO);
        raf.writeInt(dados.length);
        raf.write(dados);
    }

    // Percorre o arquivo procurando o registro com o ID informado
    public Pokemon read(int id) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "r")) {
            raf.seek(TAM_CABECALHO);

            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();

                if (lapide == VALIDO) {
                    byte[] dados = new byte[tamanho];
                    raf.readFully(dados);
                    Pokemon p = Pokemon.fromByteArray(dados);
                    if (p.getId() == id) {
                        return p;
                    }
                } else {
                    raf.seek(posicaoRegistro + TAM_CABECALHO_REGISTRO + tamanho);
                }
            }
        }
        return null;
    }

    // Retorna todos os registros que ainda estão válidos (não excluídos)
    public List<Pokemon> listarTodos() throws IOException {
        List<Pokemon> lista = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "r")) {
            raf.seek(TAM_CABECALHO);
            while (raf.getFilePointer() < raf.length()) {
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();
                byte[] dados = new byte[tamanho];
                raf.readFully(dados);
                if (lapide == VALIDO) {
                    lista.add(Pokemon.fromByteArray(dados));
                }
            }
        }
        return lista;
    }

    // Procura o registro pelo ID e atualiza os dados dele
    public boolean update(int id, Pokemon novosDados) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "rw")) {
            raf.seek(TAM_CABECALHO);

            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanhoAntigo = raf.readInt();

                if (lapide == VALIDO) {
                    byte[] dadosAntigos = new byte[tamanhoAntigo];
                    raf.readFully(dadosAntigos);
                    Pokemon existente = Pokemon.fromByteArray(dadosAntigos);

                    if (existente.getId() == id) {
                        novosDados.setId(id);
                        byte[] dadosNovos = novosDados.toByteArray();

                        // se o tamanho não mudou, só sobrescreve no mesmo lugar
                        if (dadosNovos.length == tamanhoAntigo) {
                            raf.seek(posicaoRegistro + TAM_CABECALHO_REGISTRO);
                            raf.write(dadosNovos);
                        } else {
                            // se o tamanho mudou, apaga o antigo e escreve no final
                            raf.seek(posicaoRegistro);
                            raf.writeByte(EXCLUIDO);

                            raf.seek(raf.length());
                            raf.writeByte(VALIDO);
                            raf.writeInt(dadosNovos.length);
                            raf.write(dadosNovos);
                        }
                        return true;
                    }
                } else {
                    raf.seek(posicaoRegistro + TAM_CABECALHO_REGISTRO + tamanhoAntigo);
                }
            }
        }
        return false;
    }

    // Procura o registro pelo ID e marca ele como excluído (lápide)
    public boolean delete(int id) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(caminhoArquivo, "rw")) {
            raf.seek(TAM_CABECALHO);

            while (raf.getFilePointer() < raf.length()) {
                long posicaoRegistro = raf.getFilePointer();
                byte lapide = raf.readByte();
                int tamanho = raf.readInt();

                if (lapide == VALIDO) {
                    byte[] dados = new byte[tamanho];
                    raf.readFully(dados);
                    Pokemon existente = Pokemon.fromByteArray(dados);

                    if (existente.getId() == id) {
                        raf.seek(posicaoRegistro);
                        raf.writeByte(EXCLUIDO);
                        return true;
                    }
                } else {
                    raf.seek(posicaoRegistro + TAM_CABECALHO_REGISTRO + tamanho);
                }
            }
        }
        return false;
    }

    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }
}