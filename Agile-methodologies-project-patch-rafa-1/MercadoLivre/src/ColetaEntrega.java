import java.util.Scanner;
import java.util.function.UnaryOperator;

/**
 * Coleta dos dados de entrega pelo terminal.
 *
 * Pede campo a campo e repete a pergunta enquanto o valor for inválido
 * (vazio, CEP errado, etc.). O usuário pode digitar "cancelar" para desistir.
 * Card do Trello: "Implementar coleta dos dados de entrega".
 */
public class ColetaEntrega {

    /** Sinaliza que o usuário desistiu (ou que a entrada acabou). */
    private static class EntradaCancelada extends RuntimeException {
    }

    /**
     * Pergunta um campo até o validador aceitar o valor.
     *
     * @param validador método de DadosEntrega que limpa/valida o texto
     */
    private static String lerCampo(Scanner s, String rotulo, UnaryOperator<String> validador) {
        while (true) {
            System.out.print(rotulo + ": ");
            if (!s.hasNextLine()) {
                throw new EntradaCancelada();
            }
            String entrada = s.nextLine();
            if (entrada.trim().equalsIgnoreCase("cancelar")) {
                throw new EntradaCancelada();
            }
            try {
                return validador.apply(entrada);
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage());
            }
        }
    }

    /**
     * Coleta todos os dados de entrega.
     *
     * @return os dados validados, ou null se o usuário cancelou
     */
    public static DadosEntrega coletar(Scanner s) {
        System.out.println("=========== DADOS DE ENTREGA ===========");
        System.out.println("(digite 'cancelar' em qualquer campo para desistir)");
        try {
            String nome = lerCampo(s, "Nome do destinatário", DadosEntrega::validarNome);
            String telefone = lerCampo(s, "Telefone com DDD", DadosEntrega::validarTelefone);
            String cep = lerCampo(s, "CEP", DadosEntrega::validarCep);
            String logradouro = lerCampo(s, "Rua/Avenida", DadosEntrega::validarLogradouro);
            String numero = lerCampo(s, "Número", DadosEntrega::validarNumero);
            String complemento = lerCampo(s, "Complemento (opcional)", DadosEntrega::validarComplemento);
            String bairro = lerCampo(s, "Bairro", DadosEntrega::validarBairro);
            String cidade = lerCampo(s, "Cidade", DadosEntrega::validarCidade);
            String estado = lerCampo(s, "Estado (sigla)", DadosEntrega::validarEstado);

            return new DadosEntrega(nome, telefone, cep, logradouro, numero,
                    complemento, bairro, cidade, estado);
        } catch (EntradaCancelada e) {
            System.out.println("Preenchimento dos dados de entrega cancelado.");
            return null;
        }
    }
}
