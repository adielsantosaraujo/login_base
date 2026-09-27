package com.example.loginbase.jogo.dominio;

import java.lang.reflect.ParameterizedType;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;

/**
 * Base para conversores JPA que serializam/desserializam um atributo como
 * JSON (Jackson) em uma coluna {@code text}. O tipo concreto é descoberto via
 * generics a partir da subclasse, então cada uso concreto deve:
 *
 * <pre>
 * &#64;Converter
 * class MeuTipoConverter extends JsonConverter&lt;MeuTipo&gt; {
 * }
 * </pre>
 *
 * e ser associado ao atributo com {@code @Convert(converter = MeuTipoConverter.class)}.
 *
 * <p>
 * Não há, por ora, nenhum atributo usando esta base: {@code Batalha.estado} e
 * {@code Batalha.loot} são gravados como JSON serializado em {@code String}
 * porque os tipos {@code EstadoBatalha} (pacote {@code jogo.masmorra.combate})
 * e {@code Loot} (pacote {@code jogo.masmorra}) ainda não existem — são
 * criados por outras tasks da mesma change. Quando esses tipos existirem,
 * bastará criar subclasses concretas desta base e trocar o tipo do atributo
 * em {@link Batalha}.
 */
public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final Class<T> tipo;

	@SuppressWarnings("unchecked")
	protected JsonConverter() {
		ParameterizedType superclasseGenerica = (ParameterizedType) getClass().getGenericSuperclass();
		this.tipo = (Class<T>) superclasseGenerica.getActualTypeArguments()[0];
	}

	@Override
	public String convertToDatabaseColumn(T atributo) {
		if (atributo == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(atributo);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao serializar " + tipo.getSimpleName() + " para JSON", e);
		}
	}

	@Override
	public T convertToEntityAttribute(String dado) {
		if (dado == null) {
			return null;
		}
		try {
			return OBJECT_MAPPER.readValue(dado, tipo);
		} catch (JacksonException e) {
			throw new IllegalStateException("Falha ao desserializar " + tipo.getSimpleName() + " de JSON", e);
		}
	}

}
