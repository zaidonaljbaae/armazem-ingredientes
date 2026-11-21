package com.vega.armazem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Requisições HTTP reais em paralelo contra o servidor embutido (porta aleatória). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConcorrenciaTest {

    private static final int THREADS = 10;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void a14_dezEntradasSimultaneasNaoPassamDaCapacidade() throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<Future<Integer>> respostas = new ArrayList<>();
        for (int i = 1; i <= THREADS; i++) {
            respostas.add(executor.submit(entradaDe100Kg(i, largada)));
        }
        largada.countDown();

        List<Integer> status = new ArrayList<>();
        for (Future<Integer> resposta : respostas) {
            status.add(resposta.get(30, TimeUnit.SECONDS));
        }
        executor.shutdown();

        Map<Integer, Long> porStatus = status.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        BigDecimal totalC1 = jdbc.queryForObject(
                "select quantidade_atual from compartimento where codigo = 'C1'", BigDecimal.class);
        Long entradas = jdbc.queryForObject("select count(*) from movimentacao", Long.class);
        System.out.printf("A14 >> status por resposta: %s | total C1: %s kg | ENTRADAS gravadas: %d%n",
                porStatus, totalC1, entradas);

        long sucessos = porStatus.getOrDefault(201, 0L) + porStatus.getOrDefault(200, 0L);
        assertThat(sucessos).isEqualTo(6L);
        assertThat(porStatus).containsEntry(400, 4L);
        assertThat(totalC1).isEqualByComparingTo("600");
        assertThat(entradas).isEqualTo(6L);
    }

    /** Nomes diferentes: cada entrada aceita cria um ingrediente novo (201). */
    private Callable<Integer> entradaDe100Kg(int numero, CountDownLatch largada) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String corpo = """
                {"nome":"Farinha %d","tipo":"SECO","quantidade":100,"compartimentoId":1,"responsavel":"thread-%d"}
                """.formatted(numero, numero);
        return () -> {
            largada.await();
            return rest.postForEntity("/ingredientes", new HttpEntity<>(corpo, headers), String.class)
                    .getStatusCode().value();
        };
    }
}
