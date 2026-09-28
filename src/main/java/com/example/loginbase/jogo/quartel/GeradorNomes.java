package com.example.loginbase.jogo.quartel;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import com.example.loginbase.jogo.config.AleatorioNomes;

/**
 * Carrega as listas de nomes e sobrenomes de {@code jogo/nomes/} no
 * classpath (design.md D1) e sorteia pares nome/sobrenome para novas
 * unidades (design.md D2).
 *
 * <p>
 * As listas são carregadas uma única vez, no construtor: se algum arquivo
 * estiver ausente ou a lista vier vazia, lança {@link IllegalStateException}
 * — falha rápido, tirando a aplicação do ar na inicialização em vez de
 * falhar silenciosamente em produção.
 *
 * <p>
 * O sorteio usa o bean {@link AleatorioNomes}, isolado do {@code Aleatorio}
 * usado por loot/IA, para não deslocar as sequências determinísticas
 * daquelas regras nos testes.
 */
@Component
public class GeradorNomes {

	private static final String CAMINHO_NOMES = "jogo/nomes/nome_pessoas.json";
	private static final String CAMINHO_SOBRENOMES = "jogo/nomes/sobrenome_pessoas.json";

	private final AleatorioNomes aleatorioNomes;
	private final List<String> nomes;
	private final List<String> sobrenomes;

	@Autowired
	public GeradorNomes(AleatorioNomes aleatorioNomes) {
		this(aleatorioNomes, CAMINHO_NOMES, CAMINHO_SOBRENOMES);
	}

	/**
	 * Construtor de pacote usado pelos testes para apontar para recursos
	 * inexistentes/inválidos e validar a falha rápida no construtor.
	 */
	GeradorNomes(AleatorioNomes aleatorioNomes, String caminhoNomes, String caminhoSobrenomes) {
		this.aleatorioNomes = aleatorioNomes;
		ObjectMapper objectMapper = new ObjectMapper();
		this.nomes = carregarLista(objectMapper, caminhoNomes);
		this.sobrenomes = carregarLista(objectMapper, caminhoSobrenomes);
	}

	private static List<String> carregarLista(ObjectMapper objectMapper, String caminho) {
		ClassPathResource recurso = new ClassPathResource(caminho);
		String[] valores;
		try (InputStream entrada = recurso.getInputStream()) {
			valores = objectMapper.readValue(entrada, String[].class);
		} catch (IOException e) {
			throw new IllegalStateException("Falha ao carregar lista de nomes do classpath: " + caminho, e);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao desserializar lista de nomes: " + caminho, e);
		}
		if (valores == null || valores.length == 0) {
			throw new IllegalStateException("Lista de nomes vazia: " + caminho);
		}
		return List.of(valores);
	}

	/**
	 * Sorteia um nome e um sobrenome, independentemente entre si, das listas
	 * carregadas do classpath.
	 */
	public NomePessoa sortear() {
		String nome = nomes.get(aleatorioNomes.proximoEntre(nomes.size()));
		String sobrenome = sobrenomes.get(aleatorioNomes.proximoEntre(sobrenomes.size()));
		return new NomePessoa(nome, sobrenome);
	}

}
