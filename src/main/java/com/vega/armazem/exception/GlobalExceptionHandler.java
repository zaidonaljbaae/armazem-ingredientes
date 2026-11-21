package com.vega.armazem.exception;

import com.vega.armazem.domain.TipoIngrediente;
import com.vega.armazem.dto.ErroResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/** Todos os erros saem com o mesmo JSON: timestamp, status, error, message, path. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return erro(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> campoInvalido(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return erro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    /** JSON mal formado ou valor que não converte (ex.: tipo inexistente). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable causa = causaRaiz(ex);
        String mensagem = causa instanceof IllegalArgumentException
                ? causa.getMessage()
                : "Corpo da requisição inválido ou mal formatado.";
        return erro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroFaltando(MissingServletRequestParameterException ex,
                                                         HttpServletRequest request) {
        return erro(HttpStatus.BAD_REQUEST,
                "O parâmetro '" + ex.getParameterName() + "' é obrigatório.", request);
    }

    /** Parâmetro de URL que não converte: UUID mal formatado, número ou tipo inválido. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex,
                                                         HttpServletRequest request) {
        String mensagem = ex.getRequiredType() == TipoIngrediente.class
                ? causaRaiz(ex).getMessage()
                : "Valor inválido para '" + ex.getName() + "': '" + ex.getValue() + "'.";
        return erro(HttpStatus.BAD_REQUEST, mensagem, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResponse> conflito(ObjectOptimisticLockingFailureException ex,
                                                HttpServletRequest request) {
        return erro(HttpStatus.CONFLICT,
                "O registro foi alterado por outra operação ao mesmo tempo. Tente novamente.", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return erro(HttpStatus.NOT_FOUND, "Endereço não encontrado: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoSuportado(HttpRequestMethodNotSupportedException ex,
                                                          HttpServletRequest request) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED,
                "Método " + ex.getMethod() + " não é suportado neste endereço.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente mais tarde.", request);
    }

    private ResponseEntity<ErroResponse> erro(HttpStatus status, String mensagem, HttpServletRequest request) {
        ErroResponse corpo = new ErroResponse(LocalDateTime.now(clock), status.value(),
                status.getReasonPhrase(), mensagem, request.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }

    private static Throwable causaRaiz(Throwable ex) {
        Throwable atual = ex;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
    }
}
