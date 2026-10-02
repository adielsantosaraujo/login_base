function lerCookie(nome: string): string | null {
  const par = document.cookie.split('; ').find((c) => c.startsWith(`${nome}=`))
  return par ? decodeURIComponent(par.substring(nome.length + 1)) : null
}

async function requisitar<T>(metodo: string, url: string, corpo?: unknown): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  if (corpo !== undefined) headers['Content-Type'] = 'application/json'
  if (metodo !== 'GET') {
    const xsrf = lerCookie('XSRF-TOKEN')
    if (xsrf) headers['X-XSRF-TOKEN'] = xsrf
  }

  const resposta = await fetch(url, {
    method: metodo,
    credentials: 'include',
    headers,
    body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
  })

  if (resposta.status === 401) {
    window.location.href = '/login'
    throw new Error('Não autenticado')
  }

  const texto = await resposta.text()
  let dados: unknown = null
  if (texto) {
    try {
      dados = JSON.parse(texto)
    } catch {
      dados = null
    }
  }

  if (!resposta.ok) {
    const mensagem = (dados as { erro?: string } | null)?.erro
    throw new Error(mensagem ?? `Erro ${resposta.status}`)
  }
  return dados as T
}

export const get = <T>(url: string) => requisitar<T>('GET', url)
export const post = <T>(url: string, corpo?: unknown) => requisitar<T>('POST', url, corpo)
export const put = <T>(url: string, corpo?: unknown) => requisitar<T>('PUT', url, corpo)
export const patch = <T>(url: string, corpo?: unknown) => requisitar<T>('PATCH', url, corpo)
export const del = <T>(url: string) => requisitar<T>('DELETE', url)
