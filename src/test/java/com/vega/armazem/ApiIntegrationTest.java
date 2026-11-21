package com.vega.armazem;

import com.jayway.jsonpath.JsonPath;
import com.vega.armazem.support.RelogioMutavel;
import com.vega.armazem.support.RelogioTesteConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(RelogioTesteConfig.class)
class ApiIntegrationTest {

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RelogioMutavel relogio;

    @BeforeEach
    void limparBanco() {
        jdbc.update("delete from movimentacao");
        jdbc.update("delete from ingrediente");
        jdbc.update("update compartimento set tipo_atual = null, quantidade_atual = 0, data_ultimo_armazenamento = null");
        relogio.definir(RelogioTesteConfig.INICIO);
    }

    @Test
    void a03_postValidoRetorna201DevolveSecoEGravaEntrada() throws Exception {
        mockMvc.perform(post("/ingredientes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Farinha de Trigo","tipo":"SECOS","quantidade":400,"compartimentoId":1,"responsavel":"pedro"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/ingredientes/" + UUID_REGEX)))
                .andExpect(jsonPath("$.id").value(matchesPattern(UUID_REGEX)))
                .andExpect(jsonPath("$.tipo").value("SECO"))
                .andExpect(jsonPath("$.quantidade").value(400.0))
                .andExpect(jsonPath("$.compartimento").value("C1"));

        mockMvc.perform(get("/historico"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].operacao").value("ENTRADA"))
                .andExpect(jsonPath("$[0].quantidade").value(400.0));
    }

    @Test
    void a04_capacidadeExcedidaRetorna400ComEspacoLivre() throws Exception {
        entrada("Farinha", "SECO", "400", 1).andExpect(status().isCreated());

        entrada("Acucar", "SECO", "300", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("livre de 200 kg")));
    }

    @Test
    void a05_tipoDiferenteNoMesmoDiaRetorna400() throws Exception {
        entrada("Farinha", "SECO", "100", 1).andExpect(status().isCreated());

        entrada("Leite", "LIQUIDO", "10", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void a06_saidaReduzEstoqueEGravaSaidaESaidaMaiorRetorna400() throws Exception {
        String id = idDe(entrada("Farinha", "SECO", "100", 1));

        saida(id, "30")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(70.0));
        mockMvc.perform(get("/historico").param("order", "asc"))
                .andExpect(jsonPath("$[*].operacao", contains("ENTRADA", "SAIDA")))
                .andExpect(jsonPath("$[1].quantidade").value(30.0));

        saida(id, "71").andExpect(status().isBadRequest());
        mockMvc.perform(get("/ingredientes/" + id)).andExpect(jsonPath("$.quantidade").value(70.0));
    }

    @Test
    void a07_capacidadeExcedidaNaoGravaNada() throws Exception {
        entrada("Farinha", "SECO", "500", 1).andExpect(status().isCreated());

        entrada("Acucar", "SECO", "200", 1).andExpect(status().isBadRequest());

        assertThat(jdbc.queryForObject("select count(*) from ingrediente where nome = 'Acucar'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("select count(*) from movimentacao", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select quantidade_atual from compartimento where codigo = 'C1'",
                java.math.BigDecimal.class)).isEqualByComparingTo("500");
    }

    @Test
    void a08_volumeSempreComTresTipos() throws Exception {
        mockMvc.perform(get("/ingredientes/volume"))
                .andExpect(jsonPath("$[*].type", contains("LIQUIDO", "REFRIGERADO", "SECO")))
                .andExpect(jsonPath("$[*].totalQuantity", contains(0.0, 0.0, 0.0)));

        entrada("Farinha", "SECO", "400", 1);
        entrada("Leite", "LIQUIDO", "200", 2);

        mockMvc.perform(get("/ingredientes/volume"))
                .andExpect(jsonPath("$[*].type", contains("LIQUIDO", "REFRIGERADO", "SECO")))
                .andExpect(jsonPath("$[*].totalQuantity", contains(200.0, 0.0, 400.0)));
    }

    @Test
    void a09_disponiveisSoComEspacoETipoAceitoHoje() throws Exception {
        entrada("Farinha", "SECO", "550", 1);                       // C1: só 50 kg livres
        entrada("Leite", "LIQUIDO", "10", 2);                       // C2: tem outro tipo
        saida(idDe(entrada("Suco", "LIQUIDO", "5", 3)), "5");       // C3: vazio, mas guardou LIQUIDO hoje

        mockMvc.perform(get("/compartimentos/disponiveis").param("quantidade", "100").param("tipo", "SECO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", contains("C4", "C5")));

        mockMvc.perform(get("/compartimentos/disponiveis").param("quantidade", "50").param("tipo", "SECO"))
                .andExpect(jsonPath("$[*].codigo", contains("C1", "C4", "C5")));
    }

    @Test
    void a10_disponiveisParaVendaSoComVolumeDoTipo() throws Exception {
        entrada("Farinha", "SECO", "100", 1);
        saida(idDe(entrada("Sal", "SECO", "10", 2)), "10");          // C2: SECO zerado
        entrada("Leite", "LIQUIDO", "5", 3);

        mockMvc.perform(get("/compartimentos/disponiveis-para-venda").param("tipo", "SECO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", contains("C1")));
    }

    @Test
    void a11_historicoOrdenaPorDataOuCompartimento() throws Exception {
        LocalDateTime t1 = LocalDateTime.of(2026, 11, 20, 8, 0);
        relogio.definir(t1);
        entrada("Leite", "LIQUIDO", "10", 2);
        entrada("Farinha", "SECO", "10", 1);                         // mesma hora: desempate pelo compartimento
        relogio.definir(t1.plusHours(1));
        entrada("Queijo", "REFRIGERADO", "10", 3);
        relogio.definir(t1.plusHours(2));
        entrada("Farinha", "SECO", "5", 1);

        mockMvc.perform(get("/historico"))                           // padrão: date desc
                .andExpect(jsonPath("$[*].compartimento", contains("C1", "C3", "C1", "C2")));
        mockMvc.perform(get("/historico").param("sortBy", "date").param("order", "asc"))
                .andExpect(jsonPath("$[*].compartimento", contains("C1", "C2", "C3", "C1")));
        mockMvc.perform(get("/historico").param("sortBy", "compartimento").param("order", "asc"))
                .andExpect(jsonPath("$[*].compartimento", contains("C1", "C1", "C2", "C3")))
                .andExpect(jsonPath("$[0].quantidade").value(5.0));  // desempate: data mais recente primeiro
        mockMvc.perform(get("/historico").param("sortBy", "compartimento").param("order", "desc"))
                .andExpect(jsonPath("$[*].compartimento", contains("C3", "C2", "C1", "C1")));

        mockMvc.perform(get("/historico").param("sortBy", "x")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/historico").param("order", "up")).andExpect(status().isBadRequest());
    }

    @Test
    void a12_tipoDiferenteSoAmanha() throws Exception {
        relogio.definir(LocalDateTime.of(2026, 11, 20, 23, 59));
        String id = idDe(entrada("Farinha", "SECO", "100", 1));
        saida(id, "100").andExpect(status().isOk());

        entrada("Leite", "LIQUIDO", "10", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("amanh")));

        relogio.definir(LocalDateTime.of(2026, 11, 21, 0, 1));
        entrada("Leite", "LIQUIDO", "10", 1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("LIQUIDO"));
    }

    @Test
    void a22_movimentacaoGravaCamposObrigatorios() throws Exception {
        entrada("Farinha", "SECO", "12.5", 1);

        mockMvc.perform(get("/historico"))
                .andExpect(jsonPath("$[0].dataHora").value(containsString("2026-11-20T10:00")))
                .andExpect(jsonPath("$[0].operacao").value("ENTRADA"))
                .andExpect(jsonPath("$[0].quantidade").value(12.5))
                .andExpect(jsonPath("$[0].tipo").value("SECO"))
                .andExpect(jsonPath("$[0].responsavel").value("pedro"));
    }

    @Test
    void a23_postComCamposInvalidosRetorna400() throws Exception {
        String nomeLongo = "a".repeat(101);
        String[] corpos = {
                "{\"tipo\":\"SECO\",\"quantidade\":1,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"quantidade\":1,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"quantidade\":1,\"compartimentoId\":1}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"quantidade\":0,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"quantidade\":-5,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"quantidade\":1.2345,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"" + nomeLongo + "\",\"tipo\":\"SECO\",\"quantidade\":1,\"compartimentoId\":1,\"responsavel\":\"pedro\"}",
                "{\"nome\":\"X\",\"tipo\":\"SECO\",\"quantidade\":1,\"compartimentoId\":1,\"responsavel\":\"" + nomeLongo + "\"}"
        };
        for (String corpo : corpos) {
            mockMvc.perform(post("/ingredientes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest());
        }
        assertThat(jdbc.queryForObject("select count(*) from movimentacao", Long.class)).isZero();
    }

    @Test
    void a24_listarEBuscarIngrediente() throws Exception {
        String id = idDe(entrada("Farinha", "SECO", "10", 1));
        entrada("Leite", "LIQUIDO", "10", 2);

        mockMvc.perform(get("/ingredientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/ingredientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value("Farinha"));
        mockMvc.perform(get("/ingredientes/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mockMvc.perform(get("/ingredientes/nao-e-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void a25_doisPostsComMesmoNomeSomamNoMesmoIngrediente() throws Exception {
        String id = idDe(entrada("Farinha", "SECO", "100", 1).andExpect(status().isCreated()));

        entrada("Farinha", "SECO", "100", 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.quantidade").value(200.0));

        assertThat(jdbc.queryForObject("select count(*) from ingrediente", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select count(*) from movimentacao where operacao = 'ENTRADA'", Long.class))
                .isEqualTo(2L);
    }

    @Test
    void a26_compartimentoNaRespostaTemCamposCompletos() throws Exception {
        entrada("Farinha", "SECO", "100", 1);

        mockMvc.perform(get("/compartimentos"))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].id").value(matchesPattern(UUID_REGEX)))
                .andExpect(jsonPath("$[0].codigo").value("C1"))
                .andExpect(jsonPath("$[0].tipoAtual").value("SECO"))
                .andExpect(jsonPath("$[0].capacidade").value(600.0))
                .andExpect(jsonPath("$[0].quantidadeAtual").value(100.0))
                .andExpect(jsonPath("$[0].espacoLivre").value(500.0))
                .andExpect(jsonPath("$[1].codigo").value("C2"))
                .andExpect(jsonPath("$[1].tipoAtual").value(nullValue()));
    }

    // ---------- auxiliares ----------

    private ResultActions entrada(String nome, String tipo, String quantidade, int compartimentoId) throws Exception {
        return mockMvc.perform(post("/ingredientes").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"%s","tipo":"%s","quantidade":%s,"compartimentoId":%d,"responsavel":"pedro"}
                """.formatted(nome, tipo, quantidade, compartimentoId)));
    }

    private ResultActions saida(String ingredienteId, String quantidade) throws Exception {
        return mockMvc.perform(post("/ingredientes/" + ingredienteId + "/saida")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidade\":%s,\"responsavel\":\"maria\"}".formatted(quantidade)));
    }

    private static String idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
