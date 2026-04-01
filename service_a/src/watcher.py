"""
watcher.py
Monitors data/input/ for new CSV files and triggers a processing callback.

Uses the *watchdog* library for efficient filesystem events.
Falls back to polling every 2 s when watchdog is unavailable.
"""

import os
import time
import logging
from pathlib import Path

logger = logging.getLogger(__name__)

WATCH_DIR    = "data/input"
POLL_INTERVAL = 2  # seconds between scan cycles (polling fallback)


# ── watchdog-based handler ────────────────────────────────────────────────────

def _make_handler(callback, processed: set):
    """Return a watchdog FileSystemEventHandler that calls *callback* on new CSVs."""
    try:
        from watchdog.events import FileSystemEventHandler

        class _CSVHandler(FileSystemEventHandler):
            def on_created(self, event):
                if event.is_directory:
                    return
                path = Path(event.src_path)
                if path.suffix.lower() == ".csv" and str(path) not in processed:
                    processed.add(str(path))
                    logger.info("New CSV detected (watchdog): %s", path)
                    callback(str(path))

        return _CSVHandler()
    except ImportError:
        return None


# ── public API ────────────────────────────────────────────────────────────────

def watch(
    callback,
    watch_dir: str = WATCH_DIR,
    stop_after_first: bool = True,
    pre_seed: bool = True,
):
    """
    Monitor *watch_dir* for new CSV files and call *callback(filepath)*.

    Parameters
    ----------
    callback         : callable(filepath: str) – called when a new CSV is found
    watch_dir        : directory to monitor
    stop_after_first : if True, stop after the first detected file
                       (handy for the one-shot execution flow in main.py)
    pre_seed         : if True, existing files at start-up are ignored;
                       set to False to also process files that already exist
    """
    os.makedirs(watch_dir, exist_ok=True)
    processed: set[str] = set()

    # Optionally snapshot existing files so we only react to *new* ones
    if pre_seed:
        for f in Path(watch_dir).glob("*.csv"):
            processed.add(str(f))

    try:
        from watchdog.observers import Observer

        handler  = _make_handler(callback, processed)
        observer = Observer()
        observer.schedule(handler, watch_dir, recursive=False)
        observer.start()
        logger.info("Watchdog observer started → monitoring '%s'", watch_dir)

        try:
            while True:
                time.sleep(0.5)
                if stop_after_first and len(processed) > len(set(
                    str(f) for f in Path(watch_dir).glob("*.csv")
                ) - set(processed)):
                    break
                # Check whether callback was triggered for any new file
                current = {str(f) for f in Path(watch_dir).glob("*.csv")}
                if stop_after_first and current - (current & processed):
                    # watchdog handler will deal with it; just keep looping
                    pass
                if stop_after_first and all(
                    str(f) in processed for f in Path(watch_dir).glob("*.csv")
                ) and len(processed) > 0:
                    break
        finally:
            observer.stop()
            observer.join()

    except ImportError:
        logger.warning("watchdog not installed – falling back to polling.")
        _poll(callback, watch_dir, processed, stop_after_first)


def _poll(callback, watch_dir: str, processed: set, stop_after_first: bool):
    """Simple polling fallback when watchdog is unavailable."""
    logger.info("Polling '%s' every %ds …", watch_dir, POLL_INTERVAL)
    while True:
        for path in Path(watch_dir).glob("*.csv"):
            key = str(path)
            if key not in processed:
                processed.add(key)
                logger.info("New CSV detected (poll): %s", path)
                callback(key)
                if stop_after_first:
                    return
        time.sleep(POLL_INTERVAL)
