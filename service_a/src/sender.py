"""
sender.py
Reads valid_data.csv and POSTs student records to the Spring Boot API
in configurable-size batches with retry logic.

The backend URL is resolved from the BACKEND_URL environment variable
(default: http://backend:8080/students/bulk) so that Docker service-name
routing works correctly without any hardcoded 'localhost' references.

Notes
-----
- The 'id' field is intentionally stripped before sending; Spring Boot
  auto-generates IDs via the database sequence.
- Batch size: 30 records per request.
- Retry logic: up to 3 attempts with a 2-second delay between each.
"""

import csv
import os
import time
import logging
import requests

logger = logging.getLogger(__name__)

# Resolved from environment so Docker service-name routing works correctly.
# Override via:  BACKEND_URL=http://backend:8080/students/bulk
API_URL     = os.environ.get("BACKEND_URL", "http://backend:8080/students/bulk")
BATCH_SIZE  = 30      # records per request
MAX_RETRIES = 3       # number of additional attempts after first failure
RETRY_DELAY = 2.0     # seconds to wait between retries


# ── helpers ───────────────────────────────────────────────────────────────────

# Fields the Spring Boot API accepts – 'id' is excluded (auto-generated)
_API_FIELDS = ("name", "email", "age")


def _read_csv(path: str) -> list[dict]:
    """
    Read CSV into a list of clean dicts.

    Only the fields accepted by the Spring Boot API are kept
    (name, email, age).  The 'id' column is intentionally dropped
    because Spring Boot auto-generates IDs via the DB sequence.
    Age is cast to int so the JSON payload is typed correctly.
    """
    records: list[dict] = []
    with open(path, newline="", encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            try:
                age = int(float(row.get("age", "0")))
            except (ValueError, TypeError):
                age = 0
            records.append({
                "name":  row.get("name", "").strip(),
                "email": row.get("email", "").strip(),
                "age":   age,
            })
    return records


# HTTP headers required by the Spring Boot endpoint
_HEADERS = {"Content-Type": "application/json"}


def _post_batch(
    batch: list[dict],
    api_url: str,
    batch_num: int = 1,
    total_batches: int = 1,
) -> bool:
    """
    POST a single batch to the Spring Boot API.

    Parameters
    ----------
    batch         : list of record dicts (name/email/age only – no id)
    api_url       : full URL of the bulk-ingest endpoint
    batch_num     : 1-based index of this batch (for logging)
    total_batches : total number of batches (for logging)

    Returns True on success, False after all retries are exhausted.
    """
    attempt = 0
    while attempt <= MAX_RETRIES:
        try:
            if attempt == 0:
                # First try – show batch progress
                logger.info(
                    "[SEND] Batch %d/%d – POSTing %d record(s) to %s …",
                    batch_num, total_batches, len(batch), api_url,
                )
            else:
                # Subsequent tries – show retry counter
                logger.info(
                    "[SEND] Batch %d/%d – Retry %d/%d – POSTing %d record(s) …",
                    batch_num, total_batches, attempt, MAX_RETRIES, len(batch),
                )
            response = requests.post(
                api_url,
                json=batch,
                headers=_HEADERS,
                timeout=10,
            )
            if response.status_code in (200, 201):
                # Spring Boot may return a list or a count – handle both
                try:
                    data = response.json()
                    stored = len(data) if isinstance(data, list) else data.get("total_stored", len(batch))
                except Exception:
                    stored = "?"
                logger.info(
                    "[SEND] Batch %d/%d sent ✔  size=%d  HTTP=%d  stored=%s",
                    batch_num, total_batches, len(batch), response.status_code, stored,
                )
                return True
            else:
                logger.warning(
                    "[SEND] Batch %d/%d – HTTP %d: %s",
                    batch_num, total_batches,
                    response.status_code, response.text[:300],
                )
        except requests.ConnectionError as exc:
            logger.warning(
                "[SEND] Batch %d/%d – Connection error (is Spring Boot running?): %s",
                batch_num, total_batches, exc,
            )
        except requests.Timeout:
            logger.warning(
                "[SEND] Batch %d/%d – Request timed out after 10 s.",
                batch_num, total_batches,
            )
        except requests.RequestException as exc:
            logger.warning(
                "[SEND] Batch %d/%d – %s: %s",
                batch_num, total_batches, type(exc).__name__, exc,
            )

        attempt += 1
        if attempt <= MAX_RETRIES:
            logger.info(
                "[SEND] Batch %d/%d – Retrying in %.1f s …",
                batch_num, total_batches, RETRY_DELAY,
            )
            time.sleep(RETRY_DELAY)

    logger.error(
        "[SEND] Batch %d/%d permanently failed after %d attempt(s). Skipping %d record(s).",
        batch_num, total_batches, MAX_RETRIES + 1, len(batch),
    )
    return False


# ── public API ────────────────────────────────────────────────────────────────

def send_valid_data(
    csv_path: str  = "data/output/valid_data.csv",
    api_url: str   = API_URL,
    batch_size: int = BATCH_SIZE,
) -> dict:
    """
    Read *csv_path* and send all records to *api_url* in batches.

    Returns a summary dict: total_sent / batches_ok / batches_failed
    """
    records = _read_csv(csv_path)
    if not records:
        logger.warning("No records to send from %s", csv_path)
        return {"total_records": 0, "total_sent": 0, "batches_ok": 0, "batches_failed": 0}

    total       = len(records)
    batches_ok  = 0
    batches_fail= 0
    sent_count  = 0

    logger.info(
        "[SEND] Starting → %d record(s) in batches of %d to %s",
        total, batch_size, api_url,
    )

    total_batches = (total + batch_size - 1) // batch_size   # ceiling division

    for batch_idx, start in enumerate(range(0, total, batch_size), start=1):
        batch = records[start : start + batch_size]
        success = _post_batch(batch, api_url, batch_num=batch_idx, total_batches=total_batches)
        if success:
            batches_ok += 1
            sent_count += len(batch)
        else:
            batches_fail += 1

    summary = {
        "total_records":  total,
        "total_sent":     sent_count,
        "batches_ok":     batches_ok,
        "batches_failed": batches_fail,
    }

    logger.info(
        "Send complete → sent=%d/%d  batches_ok=%d  batches_failed=%d",
        sent_count, total, batches_ok, batches_fail,
    )
    return summary
