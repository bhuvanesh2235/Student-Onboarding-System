"""
validator.py
Reads CSVs in chunks, validates every record, and writes
valid/invalid output files.
"""

import re
import os
import logging
import pandas as pd

logger = logging.getLogger(__name__)

EMAIL_RE = re.compile(
    r"^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$"
)

VALID_OUT   = "data/output/valid_data.csv"
INVALID_OUT = "data/output/invalid_data.csv"
CHUNK_SIZE  = 50          # rows per chunk – keeps RAM low for large files


# ── row-level helpers ─────────────────────────────────────────────────────────

def _is_valid_name(value) -> bool:
    """Name must be a non-empty string."""
    return isinstance(value, str) and bool(value.strip())


def _is_valid_email(value) -> bool:
    """Email must match a basic RFC-like pattern."""
    if not isinstance(value, str):
        return False
    return bool(EMAIL_RE.match(value.strip()))


def _is_valid_age(value) -> bool:
    """Age must be convertible to int and > 0."""
    try:
        return int(float(str(value))) > 0
    except (ValueError, TypeError):
        return False


def _validate_row(row: pd.Series) -> tuple[bool, list[str]]:
    """
    Validate a single row.

    Returns
    -------
    (is_valid, reasons)
        reasons is an empty list when is_valid is True.
    """
    reasons: list[str] = []

    if not _is_valid_name(row.get("name")):
        reasons.append("invalid_name")

    if not _is_valid_email(row.get("email")):
        reasons.append("invalid_email")

    if not _is_valid_age(row.get("age")):
        reasons.append("invalid_age")

    return (len(reasons) == 0), reasons


# ── public API ────────────────────────────────────────────────────────────────

def validate_csv(
    input_path: str,
    valid_out: str   = VALID_OUT,
    invalid_out: str = INVALID_OUT,
    chunk_size: int  = CHUNK_SIZE,
) -> dict:
    """
    Read *input_path* in chunks, validate each row, and write outputs.

    Returns a summary dict with total / valid / invalid counts.
    """
    os.makedirs(os.path.dirname(valid_out),   exist_ok=True)
    os.makedirs(os.path.dirname(invalid_out), exist_ok=True)

    total_count   = 0
    valid_count   = 0
    invalid_count = 0

    # We'll collect all chunks then write once (avoids header duplication).
    valid_frames:   list[pd.DataFrame] = []
    invalid_frames: list[pd.DataFrame] = []

    seen_ids: set = set()   # used to catch duplicate IDs

    for chunk in pd.read_csv(input_path, chunksize=chunk_size, dtype=str):
        chunk = chunk.fillna("")

        valid_rows:   list[dict] = []
        invalid_rows: list[dict] = []

        for _, row in chunk.iterrows():
            total_count += 1
            is_valid, reasons = _validate_row(row)

            # Flag duplicate IDs as invalid regardless of other fields
            row_id = row.get("id", "")
            if row_id in seen_ids:
                is_valid = False
                reasons.append("duplicate_id")
            else:
                seen_ids.add(row_id)

            record = row.to_dict()
            if is_valid:
                valid_count += 1
                valid_rows.append(record)
            else:
                invalid_count += 1
                record["validation_errors"] = "|".join(reasons)
                invalid_rows.append(record)

        if valid_rows:
            valid_frames.append(pd.DataFrame(valid_rows))
        if invalid_rows:
            invalid_frames.append(pd.DataFrame(invalid_rows))

    # ── write outputs ─────────────────────────────────────────────────────────
    if valid_frames:
        pd.concat(valid_frames, ignore_index=True).to_csv(valid_out, index=False)
        logger.info("Valid records saved → %s", valid_out)
    else:
        logger.warning("No valid records found.")

    if invalid_frames:
        pd.concat(invalid_frames, ignore_index=True).to_csv(invalid_out, index=False)
        logger.info("Invalid records saved → %s", invalid_out)
    else:
        logger.info("No invalid records found.")

    summary = {
        "total":   total_count,
        "valid":   valid_count,
        "invalid": invalid_count,
    }

    logger.info(
        "Validation summary → total=%d  valid=%d  invalid=%d",
        total_count, valid_count, invalid_count,
    )
    return summary
