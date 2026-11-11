import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conversor JSON pequeno para evitar dependências externas neste projeto.
 * Suporta objetos, listas, textos, números, booleanos e null.
 */
public final class Json {

    private Json() {
    }

    public static Map<String, Object> parseObject(String texto) {
        Object valor = new Parser(texto).parse();
        if (!(valor instanceof Map)) {
            throw new IllegalArgumentException("O corpo da requisição deve ser um objeto JSON.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> objeto = (Map<String, Object>) valor;
        return objeto;
    }

    public static String stringify(Object valor) {
        StringBuilder saida = new StringBuilder();
        escrever(valor, saida);
        return saida.toString();
    }

    private static void escrever(Object valor, StringBuilder saida) {
        if (valor == null) {
            saida.append("null");
        } else if (valor instanceof String || valor instanceof Character) {
            escreverTexto(valor.toString(), saida);
        } else if (valor instanceof Number || valor instanceof Boolean) {
            saida.append(valor);
        } else if (valor instanceof Map) {
            saida.append('{');
            boolean primeiro = true;
            for (Object entradaObj : ((Map<?, ?>) valor).entrySet()) {
                Map.Entry<?, ?> entrada = (Map.Entry<?, ?>) entradaObj;
                if (!primeiro) saida.append(',');
                primeiro = false;
                escreverTexto(String.valueOf(entrada.getKey()), saida);
                saida.append(':');
                escrever(entrada.getValue(), saida);
            }
            saida.append('}');
        } else if (valor instanceof Iterable) {
            saida.append('[');
            boolean primeiro = true;
            for (Object item : (Iterable<?>) valor) {
                if (!primeiro) saida.append(',');
                primeiro = false;
                escrever(item, saida);
            }
            saida.append(']');
        } else {
            escreverTexto(valor.toString(), saida);
        }
    }

    private static void escreverTexto(String texto, StringBuilder saida) {
        saida.append('"');
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '"': saida.append("\\\""); break;
                case '\\': saida.append("\\\\"); break;
                case '\b': saida.append("\\b"); break;
                case '\f': saida.append("\\f"); break;
                case '\n': saida.append("\\n"); break;
                case '\r': saida.append("\\r"); break;
                case '\t': saida.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        saida.append(String.format("\\u%04x", (int) c));
                    } else {
                        saida.append(c);
                    }
            }
        }
        saida.append('"');
    }

    private static final class Parser {
        private final String texto;
        private int posicao;

        private Parser(String texto) {
            this.texto = texto == null ? "" : texto;
        }

        private Object parse() {
            ignorarEspacos();
            Object valor = valor();
            ignorarEspacos();
            if (posicao != texto.length()) {
                erro("Há conteúdo extra após o JSON.");
            }
            return valor;
        }

        private Object valor() {
            ignorarEspacos();
            if (posicao >= texto.length()) erro("JSON vazio ou incompleto.");

            char atual = texto.charAt(posicao);
            if (atual == '{') return objeto();
            if (atual == '[') return lista();
            if (atual == '"') return texto();
            if (atual == 't') return palavra("true", Boolean.TRUE);
            if (atual == 'f') return palavra("false", Boolean.FALSE);
            if (atual == 'n') return palavra("null", null);
            if (atual == '-' || Character.isDigit(atual)) return numero();

            erro("Valor JSON inválido.");
            return null;
        }

        private Map<String, Object> objeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            esperar('{');
            ignorarEspacos();
            if (aceitar('}')) return mapa;

            while (true) {
                ignorarEspacos();
                if (posicao >= texto.length() || texto.charAt(posicao) != '"') {
                    erro("A chave do objeto JSON deve ser um texto.");
                }
                String chave = texto();
                ignorarEspacos();
                esperar(':');
                mapa.put(chave, valor());
                ignorarEspacos();
                if (aceitar('}')) return mapa;
                esperar(',');
            }
        }

        private List<Object> lista() {
            List<Object> itens = new ArrayList<>();
            esperar('[');
            ignorarEspacos();
            if (aceitar(']')) return itens;

            while (true) {
                itens.add(valor());
                ignorarEspacos();
                if (aceitar(']')) return itens;
                esperar(',');
            }
        }

        private String texto() {
            esperar('"');
            StringBuilder saida = new StringBuilder();
            while (posicao < texto.length()) {
                char c = texto.charAt(posicao++);
                if (c == '"') return saida.toString();
                if (c == '\\') {
                    if (posicao >= texto.length()) erro("Escape incompleto no JSON.");
                    char escape = texto.charAt(posicao++);
                    switch (escape) {
                        case '"': saida.append('"'); break;
                        case '\\': saida.append('\\'); break;
                        case '/': saida.append('/'); break;
                        case 'b': saida.append('\b'); break;
                        case 'f': saida.append('\f'); break;
                        case 'n': saida.append('\n'); break;
                        case 'r': saida.append('\r'); break;
                        case 't': saida.append('\t'); break;
                        case 'u':
                            if (posicao + 4 > texto.length()) erro("Unicode incompleto no JSON.");
                            try {
                                saida.append((char) Integer.parseInt(texto.substring(posicao, posicao + 4), 16));
                            } catch (NumberFormatException erro) {
                                erro("Sequência Unicode inválida.");
                            }
                            posicao += 4;
                            break;
                        default: erro("Escape inválido no JSON.");
                    }
                } else {
                    if (c < 0x20) erro("Caractere de controle inválido no JSON.");
                    saida.append(c);
                }
            }
            erro("Texto JSON não foi fechado.");
            return "";
        }

        private BigDecimal numero() {
            int inicio = posicao;
            if (aceitar('-')) {
                // sinal negativo
            }
            digitosObrigatorios();
            if (aceitar('.')) digitosObrigatorios();
            if (aceitar('e') || aceitar('E')) {
                if (!aceitar('+')) aceitar('-');
                digitosObrigatorios();
            }
            try {
                return new BigDecimal(texto.substring(inicio, posicao));
            } catch (NumberFormatException erro) {
                erro("Número inválido no JSON.");
                return BigDecimal.ZERO;
            }
        }

        private Object palavra(String palavra, Object retorno) {
            if (!texto.startsWith(palavra, posicao)) erro("Token JSON inválido.");
            posicao += palavra.length();
            return retorno;
        }

        private void digitosObrigatorios() {
            int inicio = posicao;
            while (posicao < texto.length() && Character.isDigit(texto.charAt(posicao))) {
                posicao++;
            }
            if (inicio == posicao) erro("Número incompleto no JSON.");
        }

        private void ignorarEspacos() {
            while (posicao < texto.length() && Character.isWhitespace(texto.charAt(posicao))) {
                posicao++;
            }
        }

        private boolean aceitar(char esperado) {
            if (posicao < texto.length() && texto.charAt(posicao) == esperado) {
                posicao++;
                return true;
            }
            return false;
        }

        private void esperar(char esperado) {
            if (!aceitar(esperado)) erro("Era esperado o caractere '" + esperado + "'.");
        }

        private void erro(String mensagem) {
            throw new IllegalArgumentException(mensagem + " Posição: " + posicao + ".");
        }
    }
}
