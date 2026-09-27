package com.example.loginbase.jogo.economia;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.catalogo.Cultivo;
import com.example.loginbase.jogo.catalogo.ModeloItem;
import com.example.loginbase.jogo.catalogo.TipoPredio;
import com.example.loginbase.jogo.catalogo.TipoTropa;
import com.example.loginbase.jogo.dominio.Canteiro;
import com.example.loginbase.jogo.dominio.CanteiroRepository;
import com.example.loginbase.jogo.dominio.Item;
import com.example.loginbase.jogo.dominio.ItemRepository;
import com.example.loginbase.jogo.dominio.Ordem;
import com.example.loginbase.jogo.dominio.OrigemItem;
import com.example.loginbase.jogo.dominio.Predio;
import com.example.loginbase.jogo.dominio.PredioRepository;
import com.example.loginbase.jogo.dominio.StatusItem;
import com.example.loginbase.jogo.dominio.StatusUnidade;
import com.example.loginbase.jogo.dominio.Unidade;
import com.example.loginbase.jogo.dominio.UnidadeRepository;
import com.example.loginbase.jogo.dominio.Vila;

/**
 * Aplica o efeito de uma {@link Ordem} vencida sobre a vila, quando
 * {@link VilaService#sincronizar} a conclui: sobe o nível do prédio (e, no
 * caso de {@code FAZENDA}, cria um novo canteiro), entrega os itens forjados
 * ou cria a unidade treinada equipando arma e armadura. A validação e a
 * criação da ordem (débito de recursos, regras de fila) são de cada serviço
 * de ação (construção, forja, quartel) — aqui só o efeito de conclusão é
 * aplicado.
 */
@Service
@Transactional
public class AplicadorOrdens {

	private final PredioRepository predioRepository;
	private final CanteiroRepository canteiroRepository;
	private final ItemRepository itemRepository;
	private final UnidadeRepository unidadeRepository;

	public AplicadorOrdens(PredioRepository predioRepository, CanteiroRepository canteiroRepository,
			ItemRepository itemRepository, UnidadeRepository unidadeRepository) {
		this.predioRepository = predioRepository;
		this.canteiroRepository = canteiroRepository;
		this.itemRepository = itemRepository;
		this.unidadeRepository = unidadeRepository;
	}

	/**
	 * Aplica o efeito de conclusão da ordem na vila informada. Não exclui a
	 * ordem: quem chama ({@link VilaService#sincronizar}) é responsável por
	 * isso após a aplicação.
	 */
	public void aplicar(Ordem ordem, Vila vila) {
		switch (ordem.getCategoria()) {
			case CONSTRUCAO -> aplicarConstrucao(ordem, vila);
			case FORJA -> aplicarForja(ordem, vila);
			case TREINO -> aplicarTreino(ordem, vila);
		}
	}

	private void aplicarConstrucao(Ordem ordem, Vila vila) {
		TipoPredio tipo = TipoPredio.valueOf(ordem.getAlvo());
		Predio predio = predioRepository.findByVilaId(vila.getId()).stream()
				.filter(p -> p.getTipo() == tipo)
				.findFirst()
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Prédio " + tipo + " não encontrado na vila " + vila.getId() + " para concluir ordem"));
		predio.setNivel(ordem.getNivel());
		predioRepository.save(predio);

		if (tipo == TipoPredio.FAZENDA) {
			int novaPosicao = canteiroRepository.findByVilaId(vila.getId()).size() + 1;
			Canteiro canteiro = new Canteiro();
			canteiro.setVilaId(vila.getId());
			canteiro.setPosicao(novaPosicao);
			canteiro.setCultivo(Cultivo.TRIGO);
			canteiro.setPlantadoEm(ordem.getConcluiEm());
			canteiroRepository.save(canteiro);
		}
	}

	private void aplicarForja(Ordem ordem, Vila vila) {
		ModeloItem modelo = ModeloItem.valueOf(ordem.getAlvo());
		for (int i = 0; i < ordem.getQuantidade(); i++) {
			Item item = new Item();
			item.setVilaId(vila.getId());
			item.setModelo(modelo);
			item.setNivel(ordem.getNivel());
			item.setOrigem(OrigemItem.FORJA);
			item.setStatus(StatusItem.DISPONIVEL);
			itemRepository.save(item);
		}
	}

	private void aplicarTreino(Ordem ordem, Vila vila) {
		TipoTropa tipo = TipoTropa.valueOf(ordem.getAlvo());
		Item arma = marcarEquipado(ordem.getArmaItemId());
		Item armadura = marcarEquipado(ordem.getArmaduraItemId());

		Unidade unidade = new Unidade();
		unidade.setVilaId(vila.getId());
		unidade.setTipo(tipo);
		unidade.setArmaItemId(arma.getId());
		unidade.setArmaduraItemId(armadura.getId());
		unidade.setStatus(StatusUnidade.DISPONIVEL);
		unidadeRepository.save(unidade);
	}

	private Item marcarEquipado(Long itemId) {
		Item item = itemRepository.findById(itemId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Item " + itemId + " não encontrado para treino"));
		item.setStatus(StatusItem.EQUIPADO);
		return itemRepository.save(item);
	}

}
