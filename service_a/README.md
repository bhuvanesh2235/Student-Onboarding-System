# Service A – Automated Student Onboarding Engine

A standalone Python project that:

1. **Generates** synthetic student CSV data (with intentional invalid records)  
2. **Monitors** a directory for new CSV files  
3. **Validates** each file (chunked reading, regex email check, age check, duplicate IDs)  
4. **Sends** clean records to a local mock REST API in batches (with retry logic)

---

## Project Structure

```
service_a/
├── data/
│   ├── input/          ← generated CSV files land here
│   └── output/         ← valid_data.csv & invalid_data.csv written here
├── src/
│   ├── generator.py    ← CSV data generator
│   ├── validator.py    ← validation logic (chunked)
│   ├── api_mock.py     ← Flask mock REST API (Service B)
│   ├── watcher.py      ← directory watcher (watchdog / polling)
│   └── sender.py       ← batch sender with retry
├── main.py             ← entry point – runs full pipeline
├── requirements.txt
└── README.md
```

---

## Prerequisites

- Python **3.10+**

---

## Setup

### 1. Navigate to the project directory

```bash
cd service_a
```

### 2. (Recommended) Create a virtual environment

```bash
python -m venv venv

# Windows
venv\Scripts\activate

# macOS / Linux
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
```

---

## Running the Project

```bash
python main.py
```

### What happens

| Step | Action |
|------|--------|
| 1 | **Generates** `data/input/students.csv` (120 rows, ~15 % invalid) |
| 2 | **Starts** the mock REST API on `http://127.0.0.1:5000` |
| 3 | **Watches** `data/input/` for new CSV files |
| 4 | **Detects** the newly generated file |
| 5 | **Validates** records → writes `valid_data.csv` & `invalid_data.csv` |
| 6 | **Sends** valid records to `POST /students` in batches of 30 |

---

## Outputs

| File | Description |
|------|-------------|
| `data/output/valid_data.csv` | Records that passed all validations |
| `data/output/invalid_data.csv` | Rejected records with a `validation_errors` column |
| `onboarding.log` | Full run log (appended each run) |

---

## Mock API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/students` | Accept a JSON array of students |
| `GET`  | `/students` | List all stored students (debug) |
| `GET`  | `/health`   | Health-check |

### Example request

```bash
curl -X POST http://127.0.0.1:5000/students \
     -H "Content-Type: application/json" \
     -d '[{"id":"1","name":"Alice Smith","email":"alice@example.com","age":"22"}]'
```

---

## Validation Rules

| Field | Rule |
|-------|------|
| `name` | Must not be empty |
| `email` | Must match `user@domain.tld` pattern |
| `age` | Must be numeric and > 0 |
| `id` | Must be unique within the file |

---

## Configuration

Key constants you can change:

| File | Constant | Default | Meaning |
|------|----------|---------|---------|
| `src/sender.py` | `BATCH_SIZE` | `30` | Records per API request |
| `src/sender.py` | `MAX_RETRIES` | `3` | Retry attempts on failure |
| `src/sender.py` | `RETRY_DELAY` | `2.0` s | Wait between retries |
| `src/validator.py` | `CHUNK_SIZE` | `50` | CSV rows read at once |
| `main.py` | `total` | `120` | Rows to generate |
| `main.py` | `invalid_ratio` | `0.15` | Fraction of invalid rows |
