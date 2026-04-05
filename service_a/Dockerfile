# ────────────────────────────────────────────────────────────────
# Dockerfile – service_a  (Python pipeline)
#
# Build context: ./service_a
# ────────────────────────────────────────────────────────────────

# ── Stage 1: slim Python runtime ────────────────────────────────
FROM python:3.12-slim

# Prevent .pyc files and enable unbuffered stdout (logs show immediately)
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

# Install OS-level dependencies required by watchdog + pandas
RUN apt-get update && apt-get install -y --no-install-recommends \
        gcc \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies first (layer cached unless requirements change)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the entire service_a project
COPY . .

# Create data directories so the watcher and generator don't fail
RUN mkdir -p data/input data/output

# The API URL is injected via environment variable at runtime.
# Default points to the docker-compose service name "backend".
ENV BACKEND_URL=http://backend:8080/students/bulk

CMD ["python", "main.py"]
