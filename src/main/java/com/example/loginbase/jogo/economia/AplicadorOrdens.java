package com.example.loginbase.jogo.economia;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.loginbase.jogo.RecursoNaoEncontradoException;
import com.example.loginbase.jogo.catalogo.CategoriaItem;
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
import com.example.loginbase.jogo.quartel.GeradorNomes;
import com.example.loginbase.jogo.quartel.NomePessoa;
import com.example.loginbase.jogo.quartel.NumeradorNomes;

/**
 * Aplica o efeito de uma {@link Ordem} vencida sobre a vila, quando
 * {@link VilaService#sincronizar} a conclui: sobe o nível do prédio (e, no
 * caso de {@code FAZENDA}, cria os canteiros TRIGO que o novo nível acrescenta
 * a {@link TipoPredio#numeroCanteiros(int)}, se houver), entrega os itens forjados
 * ou cria as unidades do lote treinado, cada uma equipando uma arma e uma
 * armadura reservadas pela ordem, com nome/sobrenome sorteados ({@link
 * GeradorNomes}) e ordinal do par calculado pelo contador histórico da vila
 * ({@link NumeradorNomes}, design.md D11). A validação e a criação da ordem
 * (débito de recursos, regras de fila) são de cada serviço de ação
 * (construção, forja, quartel) — aqui só o efeito de conclusão é aplicado.
 */
@Service
@Transactional
public class AplicadorOrdens {

	private final PredioRepository predioRepository;
	private final CanteiroRepository canteiroRepository;
	private final ItemRepository itemRepository;
	private final UnidadeRepository unidadeRepository;
	private final GeradorNomes geradorNomes;
	private final NumeradorNomes numeradorNomes;

	public AplicadorOrdens(PredioRepository predioRepository, CanteiroRepository canteiroRepository,
			ItemRepository itemRepository, UnidadeRepository unidadeRepository, GeradorNomes geradorNomes,
			NumeradorNomes numeradorNomes) {
		this.predioRepository = predioRepository;
		this.canteiroRepository = canteiroRepository;
		this.itemRepository = itemRepository;
		this.unidadeRepository = unidadeRepository;
		this.geradorNomes = geradorNomes;
		this.numeradorNomes = numeradorNomes;
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
			int existentes = canteiroRepository.findByVilaId(vila.getId()).size();
			int total = tipo.numeroCanteiros(ordem.getNivel());
			for (int posicao = existentes + 1; posicao <= total; posicao++) {
				Canteiro canteiro = new Canteiro();
				canteiro.setVilaId(vila.getId());
				canteiro.setPosicao(posicao);
				canteiro.setCultivo(Cultivo.TRIGO);
				canteiro.setPlantadoEm(ordem.getConcluiEm());
				canteiroRepository.save(canteiro);
			}
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
		// Já ordenados por id (menor primeiro): a ordem de formação dos pares
		// segue a ordem de reserva em QuartelService.treinar.
		List<Item> itensReservados = itemRepository.findByOrdemIdOrderById(ordem.getId());

		List<Item> armas = itensReservados.stream()
				.filter(item -> item.getModelo().categoria() == CategoriaItem.ARMA)
				.toList();
		List<Item> armaduras = itensReservados.stream()
				.filter(item -> item.getModelo().categoria() == CategoriaItem.ARMADURA)
				.toList();
		if (armas.isEmpty() || armaduras.isEmpty()) {
			throw new RecursoNaoEncontradoException(
					"Arma/armadura não encontrada para concluir treino da ordem " + ordem.getId());
		}

		// Cada par (arma[i], armadura[i]) forma uma unidade do lote; N = tamanho
		// do menor grupo (em condições normais os dois têm o mesmo tamanho, igual
		// a ordem.getQuantidade()).
		int quantidade = Math.min(armas.size(), armaduras.size());
		for (int i = 0; i < quantidade; i++) {
			Item arma = armas.get(i);
			Item armadura = armaduras.get(i);

			marcarEquipado(arma);
			marcarEquipado(armadura);

			// Sorteio independente de nome/sobrenome para cada unidade do lote
			// (duplicatas permitidas entre unidades do mesmo lote); o ordinal do
			// par é obtido logo em seguida, na ordem do laço (arma de menor id
			// primeiro), via o contador histórico persistido (design.md D11).
			NomePessoa nomeSorteado = geradorNomes.sortear();
			int ordinalNome = numeradorNomes.proximoOrdinal(vila.getId(), nomeSorteado.nome(),
					nomeSorteado.sobrenome());

			Unidade unidade = new Unidade();
			unidade.setVilaId(vila.getId());
			unidade.setTipo(tipo);
			unidade.setArmaItemId(arma.getId());
			unidade.setArmaduraItemId(armadura.getId());
			unidade.setStatus(StatusUnidade.DISPONIVEL);
			unidade.setNome(nomeSorteado.nome());
			unidade.setSobrenome(nomeSorteado.sobrenome());
			unidade.setOrdinalNome(ordinalNome);
			unidadeRepository.save(unidade);
		}
	}

	private void marcarEquipado(Item item) {
		item.setStatus(StatusItem.EQUIPADO);
		// O item passa a pertencer à unidade, não mais à ordem que o reservou.
		item.setOrdemId(null);
		itemRepository.save(item);
	}

}
