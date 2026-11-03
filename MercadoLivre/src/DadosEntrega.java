/**
 * Dados do destinatário e do endereço de entrega.
 *
 * Obrigatórios: nome, telefone, CEP, logradouro, número, bairro, cidade e estado.
 * Opcional: complemento.
 * Cada campo é validado no setter (mesmo estilo do Produto).
 * Card do Trello: "Implementar coleta dos dados de entrega".
 */
public class DadosEntrega {

    private String nomeDestinatario;
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    public DadosEntrega(String nomeDestinatario, String telefone, String cep,
                        String logradouro, String numero, String complemento,
                        String bairro, String cidade, String estado) {
        setNomeDestinatario(nomeDestinatario);
        setTelefone(telefone);
        setCep(cep);
        setLogradouro(logradouro);
        setNumero(numero);
        setComplemento(complemento);
        setBairro(bairro);
        setCidade(cidade);
        setEstado(estado);
    }

    // ---------- VALIDADORES (devolvem o valor já limpo ou lançam erro) ----------

    /** Tira espaços, parênteses, pontos e hífens (para telefone e CEP). */
    private static String limparFormatacao(String valor) {
        return valor == null ? "" : valor.replaceAll("[\\s().-]", "");
    }

    public static String validarTextoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("O campo " + nomeCampo + " é obrigatório!");
        }
        return valor.trim();
    }

    public static String validarNome(String valor) {
        return validarTextoObrigatorio(valor, "nome do destinatário");
    }

    public static String validarTelefone(String valor) {
        String digitos = limparFormatacao(valor);
        if (!digitos.matches("\\d{10,11}")) {
            throw new IllegalArgumentException(
                    "O telefone deve ter DDD + número (10 ou 11 dígitos)!");
        }
        return digitos;
    }

    public static String validarCep(String valor) {
        String digitos = limparFormatacao(valor);
        if (!digitos.matches("\\d{8}")) {
            throw new IllegalArgumentException("O CEP deve ter 8 dígitos!");
        }
        return digitos.substring(0, 5) + "-" + digitos.substring(5);
    }

    public static String validarLogradouro(String valor) {
        return validarTextoObrigatorio(valor, "rua/avenida");
    }

    public static String validarNumero(String valor) {
        return validarTextoObrigatorio(valor, "número");
    }

    /** Complemento é opcional: vazio vira "". */
    public static String validarComplemento(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public static String validarBairro(String valor) {
        return validarTextoObrigatorio(valor, "bairro");
    }

    public static String validarCidade(String valor) {
        return validarTextoObrigatorio(valor, "cidade");
    }

    public static String validarEstado(String valor) {
        if (valor == null || !valor.trim().matches("[A-Za-z]{2}")) {
            throw new IllegalArgumentException("O estado deve ter a sigla com 2 letras (ex: PR)!");
        }
        return valor.trim().toUpperCase();
    }

    // ---------- GETTERS & SETTERS ----------

    public String getNomeDestinatario() { return nomeDestinatario; }
    public void setNomeDestinatario(String v) { this.nomeDestinatario = validarNome(v); }

    public String getTelefone() { return telefone; }
    public void setTelefone(String v) { this.telefone = validarTelefone(v); }

    public String getCep() { return cep; }
    public void setCep(String v) { this.cep = validarCep(v); }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String v) { this.logradouro = validarLogradouro(v); }

    public String getNumero() { return numero; }
    public void setNumero(String v) { this.numero = validarNumero(v); }

    public String getComplemento() { return complemento; }
    public void setComplemento(String v) { this.complemento = validarComplemento(v); }

    public String getBairro() { return bairro; }
    public void setBairro(String v) { this.bairro = validarBairro(v); }

    public String getCidade() { return cidade; }
    public void setCidade(String v) { this.cidade = validarCidade(v); }

    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = validarEstado(v); }

    /**
     * @return o endereço em uma linha. Ex.: "Rua das Flores, 100 - Apto 12 - Centro, Londrina/PR - CEP 86010-000"
     */
    public String getEnderecoCompleto() {
        String comp = complemento.isEmpty() ? "" : " - " + complemento;
        return logradouro + ", " + numero + comp + " - " + bairro
                + ", " + cidade + "/" + estado + " - CEP " + cep;
    }
}
