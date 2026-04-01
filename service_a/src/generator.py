"""
generator.py
Generates synthetic student CSV data with intentional invalid records.
"""

import csv
import os
import random
import string
import logging

logger = logging.getLogger(__name__)

# ── helpers ──────────────────────────────────────────────────────────────────

def _random_name():
    first = random.choice([
        "Alice", "Bob", "Carol", "David", "Eve", "Frank",
        "Grace", "Hank", "Iris", "Jack", "Kara", "Leo",
        "Mia", "Noah", "Olivia", "Paul", "Quinn", "Rose",
        "Sam", "Tina", "Uma", "Vince", "Wendy", "Xander",
        "Yara", "Zane",
    ])
    last = random.choice([
        "Smith", "Jones", "Taylor", "Brown", "Wilson", "Evans",
        "Thomas", "Roberts", "Johnson", "Lee", "Patel", "Garcia",
        "Martinez", "Anderson", "Harris", "Clark", "Lewis", "Walker",
        "Hall", "Young",
    ])
    return f"{first} {last}"


def _random_email(name: str):
    username = name.lower().replace(" ", ".") + str(random.randint(10, 999))
    domain = random.choice(["gmail.com", "yahoo.com", "outlook.com", "example.org"])
    return f"{username}@{domain}"


def _invalid_email():
    """Return a malformed e-mail address."""
    choices = [
        "abc@",              # missing domain
        "@mail.com",         # missing local part
        "nodomain",          # no @ at all
        "double@@test.com",  # double @
        "",                  # empty
        "spaces in@mail.com",
    ]
    return random.choice(choices)


def _invalid_age():
    """Return a bad age value (negative int or non-numeric string)."""
    choices = [-5, -1, 0, "abc", "N/A", "twenty", ""]
    return random.choice(choices)


# ── public API ────────────────────────────────────────────────────────────────

def generate_students(
    output_path: str = "data/input/students.csv",
    total: int = 120,
    invalid_ratio: float = 0.15,
    duplicate_ratio: float = 0.05,
):
    """
    Generate a CSV file with `total` student records.

    Parameters
    ----------
    output_path   : destination file path
    total         : total number of rows to write (including invalids)
    invalid_ratio : fraction of rows that contain invalid field values
    duplicate_ratio: fraction of rows that reuse an already-used ID
    """
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    n_invalid = int(total * invalid_ratio)
    n_duplicates = int(total * duplicate_ratio)
    n_valid = total - n_invalid - n_duplicates

    rows = []
    used_ids: list[int] = []

    # --- valid records --------------------------------------------------------
    for i in range(1, n_valid + 1):
        name = _random_name()
        rows.append({
            "id":    i,
            "name":  name,
            "email": _random_email(name),
            "age":   random.randint(17, 35),
        })
        used_ids.append(i)

    next_id = n_valid + 1

    # --- invalid records (bad fields) ----------------------------------------
    for _ in range(n_invalid):
        kind = random.choice(["name", "email", "age"])
        name = _random_name()
        row = {
            "id":    next_id,
            "name":  name,
            "email": _random_email(name),
            "age":   random.randint(17, 35),
        }
        if kind == "name":
            row["name"] = ""                  # missing name
        elif kind == "email":
            row["email"] = _invalid_email()   # bad e-mail
        else:
            row["age"] = _invalid_age()       # bad age
        rows.append(row)
        used_ids.append(next_id)
        next_id += 1

    # --- duplicate IDs -------------------------------------------------------
    for _ in range(n_duplicates):
        dup_id = random.choice(used_ids)
        name = _random_name()
        rows.append({
            "id":    dup_id,
            "name":  name,
            "email": _random_email(name),
            "age":   random.randint(17, 35),
        })

    # --- shuffle so invalids are not all at the end --------------------------
    random.shuffle(rows)

    fieldnames = ["id", "name", "email", "age"]
    with open(output_path, "w", newline="", encoding="utf-8") as fh:
        writer = csv.DictWriter(fh, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)

    logger.info(
        "CSV generated → %s  |  total=%d  valid≈%d  invalid≈%d  duplicates≈%d",
        output_path, total, n_valid, n_invalid, n_duplicates,
    )
    return output_path
