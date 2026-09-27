package com.example.loginbase.jogo.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.loginbase.jogo.CodigoErro;
import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.RegraJogoException;

import lombok.extern.slf4j.Slf4j;

/**
 * Tratamento centralizado de erros da API do jogo ({@code /api/jogo/**}):
 * converte exceções de regra de jogo e de infraestrutura em respostas
 * {@code {codigo, mensagem}} com o HTTP status apropriado (ver design.md,
 * seção 17 — API REST).
 */
@RestControllerAdvice(basePackages = "com.example.loginbase.jogo.api")
@Slf4j
public class ErroApiHandler {

	@ExceptionHandler(RegraJogoException.class)
	public ResponseEntity<ErroDto> tratar(RegraJogoException ex) {
		HttpStatus status = ex.getCodigo() == CodigoErro.TURNO_DESATUALIZADO ? HttpStatus.CONFLICT
				: HttpStatus.UNPROCESSABLE_ENTITY;
		return ResponseEntity.status(status).body(new ErroDto(ex.getCodigo().name(), ex.getMessage()));
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErroDto> tratar(ObjectOptimisticLockingFailureException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErroDto(CodigoErro.CONFLITO.name(), "A operação foi feita com base em dados desatualizados."));
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroDto> tratar(RecursoNaoEncontradoException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ErroDto(CodigoErro.NAO_ENCONTRADO.name(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroDto> tratar(MethodArgumentNotValidException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErroDto(CodigoErro.REQUISICAO_INVALIDA.name(), "Requisição inválida."));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroDto> tratar(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErroDto(CodigoErro.REQUISICAO_INVALIDA.name(), "Corpo da requisição malformado."));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErroDto> tratar(Exception ex) {
		log.error("Erro inesperado na API do jogo", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErroDto("ERRO_INTERNO", "Ocorreu um erro inesperado. Tente novamente."));
	}

}
