package com.vega.armazem.service;

import com.vega.armazem.domain.Compartimento;
import com.vega.armazem.domain.Ingrediente;
import com.vega.armazem.domain.Movimentacao;
import com.vega.armazem.domain.Operacao;
import com.vega.armazem.domain.TipoIngrediente;
import com.vega.armazem.dto.CompartimentoResponse;
import com.vega.armazem.dto.EntradaRequest;
import com.vega.armazem.dto.EntradaResultado;
import com.vega.armazem.dto.IngredienteResponse;
import com.vega.armazem.dto.MovimentacaoResponse;
import com.vega.armazem.dto.SaidaRequest;
import com.vega.armazem.dto.VolumeResponse;
import com.vega.armazem.exception.RecursoNaoEncontradoException;
import com.vega.armazem.exception.RegraNegocioException;
import com.vega.armazem.repository.CompartimentoRepository;
import com.vega.armazem.repository.IngredienteRepository;
import com.vega.armazem.repository.MovimentacaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Todas as regras de negócio do armazém. */
@Service
@Transactional(readOnly = true)
public class EstoqueService {

    public static final int QUANTIDADE_COMPARTIMENTOS = 5;
    private static final String PREFIXO_CODIGO = "C";
    private static final int CASAS_DECIMAIS = 3;

    private final CompartimentoRepository compartimentoRepository;
    private final IngredienteRepository ingredienteRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final Clock clock;

    public EstoqueService(CompartimentoRepository compartimentoRepository,
                          IngredienteRepository ingredienteRepository,
                          MovimentacaoRepository movimentacaoRepository,
                          Clock clock) {
        this.compartimentoRepository = compartimentoRepository;
        this.ingredienteRepository = ingredienteRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.clock = clock;
    }

    // ---------- Compartimentos iniciais ----------

    @Transactional
    public void criarCompartimentosIniciais() {
        IntStream.rangeClosed(1, QUANTIDADE_COMPARTIMENTOS)
                .mapToObj(EstoqueService::codigoDoNumero)
                .filter(codigo -> !compartimentoRepository.existsByCodigo(codigo))
                .forEach(codigo -> compartimentoRepository.save(new Compartimento(codigo)));
    }

    // ---------- Entrada ----------

    /**
     * Cria o ingrediente ou soma no existente (mesmo nome no mesmo compartimento)
     * e grava a ENTRADA na mesma transação.
     */
    @Transactional
    public EntradaResultado registrarEntrada(EntradaRequest request) {
        String codigo = resolverCodigo(request.compartimentoId(), request.compartimento());
        Compartimento compartimento = compartimentoRepository.findByCodigoParaAtualizar(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Compartimento " + codigo + " não encontrado."));
        TipoIngrediente tipo = request.tipo();
        BigDecimal quantidade = request.quantidade().setScale(CASAS_DECIMAIS);

        motivoParaRecusarTipo(compartimento, tipo).ifPresent(motivo -> {
            throw new RegraNegocioException(motivo);
        });
        validarEspaco(compartimento, tipo, quantidade);

        String nome = normalizarTexto(request.nome());
        Optional<Ingrediente> existente = ingredienteRepository.findByCompartimentoAndNomeIgnoreCase(compartimento, nome);
        Ingrediente ingrediente = existente.orElseGet(() -> new Ingrediente(nome, tipo, compartimento));
        // Um ingrediente zerado de um tipo anterior passa a ter o tipo atual do compartimento.
        ingrediente.setTipo(tipo);
        ingrediente.setQuantidade(ingrediente.getQuantidade().add(quantidade));

        LocalDateTime agora = LocalDateTime.now(clock);
        compartimento.setTipoAtual(tipo);
        compartimento.setQuantidadeAtual(compartimento.getQuantidadeAtual().add(quantidade));
        compartimento.setDataUltimoArmazenamento(agora);

        ingredienteRepository.save(ingrediente);
        movimentacaoRepository.save(new Movimentacao(agora, Operacao.ENTRADA, quantidade,
                normalizarTexto(request.responsavel()), ingrediente));
        return new EntradaResultado(IngredienteResponse.de(ingrediente), existente.isEmpty());
    }

    // ---------- Saída ----------

    @Transactional
    public IngredienteResponse registrarSaida(UUID ingredienteId, SaidaRequest request) {
        Long compartimentoId = ingredienteRepository.findCompartimentoIdByUuid(ingredienteId)
                .orElseThrow(() -> ingredienteNaoEncontrado(ingredienteId));
        // Trava o compartimento antes de ler o saldo do ingrediente.
        Compartimento compartimento = compartimentoRepository.findByIdParaAtualizar(compartimentoId)
                .orElseThrow(() -> ingredienteNaoEncontrado(ingredienteId));
        Ingrediente ingrediente = ingredienteRepository.findByUuid(ingredienteId)
                .orElseThrow(() -> ingredienteNaoEncontrado(ingredienteId));
        BigDecimal quantidade = request.quantidade().setScale(CASAS_DECIMAIS);

        if (quantidade.compareTo(ingrediente.getQuantidade()) > 0) {
            throw new RegraNegocioException("Saída maior que o estoque: '" + ingrediente.getNome() + "' tem "
                    + formatar(ingrediente.getQuantidade(), ingrediente.getTipo()) + " no compartimento "
                    + compartimento.getCodigo() + ", pedido " + formatar(quantidade, ingrediente.getTipo()) + ".");
        }

        ingrediente.setQuantidade(ingrediente.getQuantidade().subtract(quantidade));
        compartimento.setQuantidadeAtual(compartimento.getQuantidadeAtual().subtract(quantidade));
        movimentacaoRepository.save(new Movimentacao(LocalDateTime.now(clock), Operacao.SAIDA, quantidade,
                normalizarTexto(request.responsavel()), ingrediente));
        return IngredienteResponse.de(ingrediente);
    }

    // ---------- Consultas de ingredientes ----------

    public List<IngredienteResponse> listarIngredientes() {
        return ingredienteRepository.findAll(Sort.by("compartimento.codigo", "nome")).stream()
                .map(IngredienteResponse::de)
                .toList();
    }

    public IngredienteResponse buscarIngrediente(UUID id) {
        return ingredienteRepository.findByUuid(id)
                .map(IngredienteResponse::de)
                .orElseThrow(() -> ingredienteNaoEncontrado(id));
    }

    /** Sempre os 3 tipos, em ordem alfabética; tipo sem estoque aparece com 0. */
    public List<VolumeResponse> volumePorTipo() {
        Map<TipoIngrediente, BigDecimal> totais = ingredienteRepository.somarQuantidadePorTipo().stream()
                .collect(Collectors.toMap(IngredienteRepository.TotalPorTipo::getTipo,
                        IngredienteRepository.TotalPorTipo::getTotal));
        return Arrays.stream(TipoIngrediente.values())
                .sorted(Comparator.comparing(Enum::name))
                .map(tipo -> new VolumeResponse(tipo,
                        totais.getOrDefault(tipo, BigDecimal.ZERO).setScale(CASAS_DECIMAIS)))
                .toList();
    }

    // ---------- Consultas de compartimentos ----------

    public List<CompartimentoResponse> listarCompartimentos() {
        return compartimentoRepository.findAllByOrderByCodigoAsc().stream()
                .map(compartimento -> paraResposta(compartimento, null))
                .toList();
    }

    /** Compartimentos que aceitam o tipo hoje e têm espaço livre para a quantidade. */
    public List<CompartimentoResponse> compartimentosDisponiveis(BigDecimal quantidade, TipoIngrediente tipo) {
        if (quantidade.signum() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero.");
        }
        return compartimentoRepository.findAllByOrderByCodigoAsc().stream()
                .filter(compartimento -> motivoParaRecusarTipo(compartimento, tipo).isEmpty())
                .filter(compartimento -> espacoLivre(compartimento, tipo).compareTo(quantidade) >= 0)
                .map(compartimento -> paraResposta(compartimento, tipo))
                .toList();
    }

    public List<CompartimentoResponse> compartimentosParaVenda(TipoIngrediente tipo) {
        return compartimentoRepository.findAllByOrderByCodigoAsc().stream()
                .filter(compartimento -> compartimento.getTipoAtual() == tipo)
                .filter(compartimento -> compartimento.getQuantidadeAtual().signum() > 0)
                .map(compartimento -> paraResposta(compartimento, tipo))
                .toList();
    }

    // ---------- Histórico ----------

    /** sortBy = date | compartimento; order = asc | desc. O desempate usa o outro campo. */
    public List<MovimentacaoResponse> historico(String sortBy, String order) {
        Sort.Direction direcao = switch (order.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new RegraNegocioException("Valor inválido para 'order': '" + order + "'. Use asc ou desc.");
        };
        Sort ordem = switch (sortBy.trim().toLowerCase(Locale.ROOT)) {
            case "date" -> Sort.by(direcao, "dataHora").and(Sort.by(Sort.Direction.ASC, "compartimento.codigo"));
            case "compartimento" -> Sort.by(direcao, "compartimento.codigo").and(Sort.by(Sort.Direction.DESC, "dataHora"));
            default -> throw new RegraNegocioException(
                    "Valor inválido para 'sortBy': '" + sortBy + "'. Use date ou compartimento.");
        };
        return movimentacaoRepository.findAll(ordem.and(Sort.by(direcao, "id"))).stream()
                .map(MovimentacaoResponse::de)
                .toList();
    }

    // ---------- Regras ----------

    /**
     * Um compartimento só troca de tipo se estiver vazio e se o último armazenamento
     * foi antes de hoje ("secos hoje, outro tipo só amanhã", para todos os tipos).
     */
    private Optional<String> motivoParaRecusarTipo(Compartimento compartimento, TipoIngrediente tipo) {
        TipoIngrediente tipoAtual = compartimento.getTipoAtual();
        if (tipoAtual == null || tipoAtual == tipo) {
            return Optional.empty();
        }
        if (compartimento.getQuantidadeAtual().signum() > 0) {
            return Optional.of("O compartimento " + compartimento.getCodigo() + " contém " + tipoAtual
                    + " e não pode receber " + tipo + ".");
        }
        if (armazenouHoje(compartimento)) {
            return Optional.of("O compartimento " + compartimento.getCodigo() + " armazenou " + tipoAtual
                    + " hoje e só pode receber " + tipo + " a partir de amanhã.");
        }
        return Optional.empty();
    }

    private boolean armazenouHoje(Compartimento compartimento) {
        LocalDateTime ultimo = compartimento.getDataUltimoArmazenamento();
        return ultimo != null && ultimo.toLocalDate().equals(LocalDate.now(clock));
    }

    private void validarEspaco(Compartimento compartimento, TipoIngrediente tipo, BigDecimal quantidade) {
        BigDecimal livre = espacoLivre(compartimento, tipo);
        if (quantidade.compareTo(livre) > 0) {
            throw new RegraNegocioException("Capacidade excedida no compartimento " + compartimento.getCodigo()
                    + ": espaço livre de " + formatar(livre, tipo) + ", pedido " + formatar(quantidade, tipo)
                    + " (máximo " + formatar(tipo.getCapacidade(), tipo) + ").");
        }
    }

    /** O(1): usa o saldo guardado no compartimento, sem somar o histórico. */
    private static BigDecimal espacoLivre(Compartimento compartimento, TipoIngrediente tipo) {
        return tipo.getCapacidade().subtract(compartimento.getQuantidadeAtual());
    }

    private static String resolverCodigo(Integer compartimentoId, String compartimento) {
        String codigoInformado = compartimento == null || compartimento.isBlank()
                ? null
                : compartimento.trim().toUpperCase(Locale.ROOT);
        if (compartimentoId == null && codigoInformado == null) {
            throw new RegraNegocioException(
                    "Informe o compartimento: 'compartimentoId' (1 a 5) ou 'compartimento' (C1 a C5).");
        }
        if (compartimentoId == null) {
            return codigoInformado;
        }
        if (compartimentoId < 1 || compartimentoId > QUANTIDADE_COMPARTIMENTOS) {
            throw new RecursoNaoEncontradoException("Compartimento com id " + compartimentoId
                    + " não encontrado. Use 1 a " + QUANTIDADE_COMPARTIMENTOS + ".");
        }
        String codigoDoId = codigoDoNumero(compartimentoId);
        if (codigoInformado != null && !codigoInformado.equals(codigoDoId)) {
            throw new RegraNegocioException("'compartimentoId' " + compartimentoId + " corresponde a " + codigoDoId
                    + ", mas 'compartimento' foi informado como " + codigoInformado + ".");
        }
        return codigoDoId;
    }

    // ---------- Auxiliares ----------

    private static String codigoDoNumero(int numero) {
        return PREFIXO_CODIGO + numero;
    }

    /** Capacidade e espaço livre usam o tipo consultado ou, sem ele, o tipo atual do compartimento. */
    private static CompartimentoResponse paraResposta(Compartimento compartimento, TipoIngrediente tipoConsultado) {
        TipoIngrediente referencia = tipoConsultado != null ? tipoConsultado : compartimento.getTipoAtual();
        BigDecimal capacidade = referencia == null ? null : referencia.getCapacidade().setScale(CASAS_DECIMAIS);
        BigDecimal espacoLivre = referencia == null ? null : espacoLivre(compartimento, referencia);
        return new CompartimentoResponse(compartimento.getUuid(), compartimento.getCodigo(),
                compartimento.getTipoAtual(), capacidade, compartimento.getQuantidadeAtual(), espacoLivre);
    }

    private static String normalizarTexto(String texto) {
        return texto.trim().replaceAll("\\s+", " ");
    }

    private static String formatar(BigDecimal quantidade, TipoIngrediente tipo) {
        return quantidade.stripTrailingZeros().toPlainString() + " " + tipo.getUnidade();
    }

    private static RecursoNaoEncontradoException ingredienteNaoEncontrado(UUID id) {
        return new RecursoNaoEncontradoException("Ingrediente " + id + " não encontrado.");
    }
}
