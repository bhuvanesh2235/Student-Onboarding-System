"""
api_mock.py
A lightweight Flask server that simulates Service B.

Endpoint:
    POST /students   – accepts a JSON array of student dicts
                       stores them in memory
                       returns a success response

Run standalone:
    python src/api_mock.py
"""

import logging
import threading
from flask import Flask, request, jsonify

logger = logging.getLogger(__name__)

app = Flask(__name__)

# In-memory store (list of dicts)
_students_db: list[dict] = []
_db_lock = threading.Lock()


@app.route("/students", methods=["POST"])
def receive_students():
    """Accept a JSON array of student records."""
    data = request.get_json(silent=True)

    if data is None:
        logger.warning("POST /students – no JSON body received")
        return jsonify({"status": "error", "message": "No JSON body"}), 400

    if not isinstance(data, list):
        logger.warning("POST /students – payload is not a list")
        return jsonify({"status": "error", "message": "Expected a JSON array"}), 400

    with _db_lock:
        _students_db.extend(data)
        current_total = len(_students_db)

    batch_size = len(data)
    logger.info(
        "POST /students – received %d record(s) | total stored: %d",
        batch_size, current_total,
    )
    return jsonify({
        "status":        "success",
        "received":      batch_size,
        "total_stored":  current_total,
    }), 201


@app.route("/students", methods=["GET"])
def list_students():
    """Helper endpoint – returns all stored students (for debugging)."""
    with _db_lock:
        return jsonify({"count": len(_students_db), "students": _students_db}), 200


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200


# ── launcher helpers (used by main.py) ────────────────────────────────────────

def start_server(host: str = "127.0.0.1", port: int = 5000) -> threading.Thread:
    """
    Start the Flask server in a background daemon thread.
    Returns the thread so the caller can join it if needed.
    """
    import time, requests as _req

    thread = threading.Thread(
        target=lambda: app.run(host=host, port=port, use_reloader=False, debug=False),
        daemon=True,
    )
    thread.start()
    logger.info("Mock API server starting on http://%s:%d …", host, port)

    # Wait until the server is ready (up to 5 s)
    base_url = f"http://{host}:{port}/health"
    for _ in range(10):
        time.sleep(0.5)
        try:
            r = _req.get(base_url, timeout=1)
            if r.status_code == 200:
                logger.info("Mock API server is ready ✔")
                return thread
        except Exception:
            pass

    logger.warning("Mock API server may not have started in time.")
    return thread


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    app.run(host="127.0.0.1", port=5000, debug=True)
