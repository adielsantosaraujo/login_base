// Cliente HTTP para a API do jogo (`/api/jogo/**`). Lê o token CSRF do
// cookie `XSRF-TOKEN` (ver spec user-authentication) e o envia no header
// `X-XSRF-TOKEN` em requisições que alteram estado. Um 401 (sessão
// expirada/anônima) redireciona para `/login`, conforme spec game-frontend.

const METODOS_MUTAVEIS = ['POST', 'PUT', 'DELETE', 'PATCH']

export class ErroApi extends Error {
  codigo: string
  status: number

  constructor(codigo: string, mensagem: string, status: number) {
    super(mensagem)
    this.name = 'ErroApi'
    this.codigo = codigo
    this.status = status
  }
}

interface CorpoErro {
  codigo?: string
  mensagem?: string
}

export function lerTokenCsrf(): string | undefined {
  const linha = document.cookie.split('; ').find((item) => item.startsWith('XSRF-TOKEN='))
  const valor = linha?.split('=')[1]
  return valor ? decodeURIComponent(valor) : undefined
}

export async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers)
  const metodo = init?.method?.toUpperCase() ?? 'GET'

  if (METODOS_MUTAVEIS.includes(metodo)) {
    const token = lerTokenCsrf()
    if (token) {
      headers.set('X-XSRF-TOKEN', token)
    }
  }

  const resposta = await fetch(path, {
    credentials: 'same-origin',
    ...init,
    headers,
  })

  if (resposta.status === 401) {
    window.location.href = '/login'
    throw new ErroApi('NAO_AUTENTICADO', 'Sessão expirada. Redirecionando para o login.', 401)
  }

  if (!resposta.ok) {
    const erro: CorpoErro = await resposta.json().catch(() => ({}) as CorpoErro)
    throw new ErroApi(erro.codigo ?? 'ERRO_DESCONHECIDO', erro.mensagem ?? 'Erro desconhecido.', resposta.status)
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  return (await resposta.json()) as T
}
