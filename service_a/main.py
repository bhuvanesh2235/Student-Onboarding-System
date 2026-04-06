"""
main.py  –  Automated Student Onboarding Engine
================================================
Execution order
---------------
1. Start the directory watcher in a background thread
2. Generate a CSV file with synthetic student data → data/input/
3. Watcher detects the new file and triggers the pipeline:
   a. Validate data  → data/output/valid_data.csv / invalid_data.csv
   b. Send ONLY valid data to the Spring Boot API (batches of 30, retry ×3)

Pre-requisites
--------------
* PostgreSQL running and configured in Spring Boot application.properties
* Spring Boot student-service running (URL set via BACKEND_URL env var)
* Copy .env.example → .env and fill in values before running locally
"""

import sys
import os
import logging
import threading
import time

# ── Load .env file (for local dev; Docker injects env vars directly) ──────────
from dotenv import load_dotenv
load_dotenv()  # reads service_a/.env (or the nearest .env up the tree)

# ── Pipeline configuration from environment variables ─────────────────────────
_BACKEND_URL        = os.environ.get("BACKEND_URL",             "http://backend:8080/students/bulk")
_BATCH_SIZE         = int(os.environ.get("PIPELINE_BATCH_SIZE",       "30"))
_MAX_RETRIES        = int(os.environ.get("PIPELINE_MAX_RETRIES",      "3"))
_RETRY_DELAY        = float(os.environ.get("PIPELINE_RETRY_DELAY",    "2.0"))
_TOTAL_STUDENTS     = int(os.environ.get("PIPELINE_TOTAL_STUDENTS",   "120"))
_INVALID_RATIO      = float(os.environ.get("PIPELINE_INVALID_RATIO",  "0.15"))

# ── ensure src/ is on the import path ─────────────────────────────────────────
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "src"))

from generator import generate_students
from validator  import validate_csv
from sender     import send_valid_data
from watcher    import watch

# ── logging setup ─────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s  %(levelname)-8s  %(name)s – %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler("onboarding.log", encoding="utf-8"),
    ],
)
logger = logging.getLogger("main")


# ── pipeline event ────────────────────────────────────────────────────────────
_pipeline_done = threading.Event()


def _process_file(csv_path: str):
    """Full pipeline: validate → send."""
    logger.info("━" * 60)
    logger.info("Pipeline triggered for: %s", csv_path)
    logger.info("━" * 60)

    # Step: Validate ──────────────────────────────────────────────
    logger.info("[STEP] Validating records …")
    summary = validate_csv(
        input_path  = csv_path,
        valid_out   = "data/output/valid_data.csv",
        invalid_out = "data/output/invalid_data.csv",
    )
    logger.info(
        "[RESULT] total=%d  valid=%d  invalid=%d",
        summary["total"], summary["valid"], summary["invalid"],
    )

    # Step: Send valid records to Spring Boot API ────────────────────
    if summary["valid"] > 0:
        logger.info("[STEP] Sending valid records to Spring Boot API …")
        logger.info("[STEP] Target URL: %s", _BACKEND_URL)
        send_summary = send_valid_data(
            csv_path  = "data/output/valid_data.csv",
            api_url   = _BACKEND_URL,
            batch_size= _BATCH_SIZE,
        )
        logger.info(
            "[RESULT] sent=%d/%d  batches_ok=%d  batches_failed=%d",
            send_summary["total_sent"],
            send_summary["total_records"],
            send_summary["batches_ok"],
            send_summary["batches_failed"],
        )
    else:
        logger.warning("No valid records to send.")

    logger.info("━" * 60)
    logger.info("Pipeline complete ✔")
    logger.info("━" * 60)
    _pipeline_done.set()


# ── entry point ───────────────────────────────────────────────────────────────

def main():
    logger.info("=" * 60)
    logger.info("  Automated Student Onboarding Engine  –  Starting")
    logger.info("=" * 60)

    # 1. Start directory watcher in background ───────────────────
    # pre_seed=False → treat files that appear after launch as new.
    # Launched in its own thread so CSV generation is detected correctly.
    logger.info("[STEP 1] Starting directory watcher on data/input/ …")
    watcher_thread = threading.Thread(
        target=watch,
        kwargs={
            "callback":         _process_file,
            "watch_dir":        "data/input",
            "stop_after_first": True,
            "pre_seed":         False,
        },
        daemon=True,
    )
    watcher_thread.start()
    # Give the watcher a moment to initialise before we drop the file
    time.sleep(1.0)

    # 2. Generate CSV ─────────────────────────────────────────────
    logger.info("[STEP 2] Generating student CSV …")
    csv_path = generate_students(
        output_path  = "data/input/students.csv",
        total        = _TOTAL_STUDENTS,
        invalid_ratio= _INVALID_RATIO,
    )
    logger.info("[STEP 2] CSV ready at: %s", csv_path)

    # 3. Wait for the pipeline to finish (timeout = 60 s) ─────────
    logger.info("[STEP 3] Waiting for watcher to detect and process the file …")
    completed = _pipeline_done.wait(timeout=60)

    if completed:
        logger.info("")
        logger.info("All done!  Check:")
        logger.info("  data/output/valid_data.csv   – clean records")
        logger.info("  data/output/invalid_data.csv – rejected records")
        logger.info("  onboarding.log               – full run log")
        logger.info("")
    else:
        logger.error("Pipeline did not complete within 60 seconds.")


if __name__ == "__main__":
    main()
