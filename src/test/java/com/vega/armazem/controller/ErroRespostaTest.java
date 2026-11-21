package com.vega.armazem.controller;

import com.vega.armazem.config.ClockConfig;
import com.vega.armazem.domain.Compartimento;
import com.vega.armazem.exception.GlobalExceptionHandler;
import com.vega.armazem.exception.RecursoNaoEncontradoException;
import com.vega.armazem.service.EstoqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O 409 só acontece em corrida real de versões; aqui o serviço é simulado para provar o formato do JSON. */
@WebMvcTest(IngredienteController.class)
@Import({GlobalExceptionHandler.class, ClockConfig.class})
class ErroRespostaTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstoqueService estoqueService;

    @Test
    void a13_errosUsamMesmoJson() throws Exception {
        UUID inexistente = UUID.randomUUID();
        UUID emConflito = UUID.randomUUID();
        when(estoqueService.buscarIngrediente(inexistente))
                .thenThrow(new RecursoNaoEncontradoException("Ingrediente " + inexistente + " não encontrado."));
        when(estoqueService.registrarSaida(eq(emConflito), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Compartimento.class, 1L));

        ResultActions erro400 = mockMvc.perform(post("/ingredientes")
                .contentType(MediaType.APPLICATION_JSON).content("{}"));
        ResultActions erro404 = mockMvc.perform(get("/ingredientes/" + inexistente));
        ResultActions erro409 = mockMvc.perform(post("/ingredientes/" + emConflito + "/saida")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidade\":1,\"responsavel\":\"pedro\"}"));

        verificarFormato(erro400, 400, "/ingredientes");
        verificarFormato(erro404, 404, "/ingredientes/" + inexistente);
        verificarFormato(erro409, 409, "/ingredientes/" + emConflito + "/saida");
    }

    private static void verificarFormato(ResultActions resposta, int status, String caminho) throws Exception {
        resposta.andExpect(status().is(status))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value(caminho));
    }
}
