# ────────────────────────────────────────────────────────────────
# Dockerfile – frontend  (React → nginx)
#
# Build context: ./frontend
# Two-stage build: Node build → nginx runtime
# ────────────────────────────────────────────────────────────────

# ── Stage 1: Build the React app ────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies first (layer-cache friendly)
COPY package.json package-lock.json ./
RUN npm ci --silent

# Copy source and build the production bundle.
# REACT_APP_API_BASE_URL is left blank so that calls fall back to "/api",
# which nginx proxies to the backend container.
COPY . .
RUN npm run build

# ── Stage 2: Serve with nginx ────────────────────────────────────
FROM nginx:1.27-alpine

# Remove the default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom config
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copy the React build output
COPY --from=builder /app/build /usr/share/nginx/html

# Expose the port declared in nginx.conf
EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
