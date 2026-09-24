# Imagem de desenvolvimento do frontend: Node 26 + npm 12, mesma família Debian (trixie) do Postgres.
FROM node:26-trixie-slim

# A imagem node:26 vem com npm 11; fixamos a major 12 pedida pela spec.
RUN npm install -g npm@12

WORKDIR /app

# Copia apenas os manifestos primeiro para aproveitar o cache de camadas do Docker.
COPY package.json package-lock.json ./
RUN npm ci

# Copia o restante do projeto (o node_modules do host fica fora via .dockerignore).
COPY . .

EXPOSE 5173

# npm install no start sincroniza o node_modules do volume nomeado com o package.json
# antes de subir o servidor de dev do Vite, acessível de fora do container.
CMD ["sh", "-c", "npm install --no-audit --no-fund && npm run dev -- --host 0.0.0.0 --port 5173 --strictPort"]
