#!/usr/bin/env python3
"""Ensure flight-plan JSON fixtures are self-contained (embedded Aircraft + airports).

Same contract as Java FlightPlanJsonExporter / US121: no runtime XML lookup.
Reference plans supply embedded blocks; this script copies missing pieces into fixtures.
"""

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

REFERENCE_PLANS = [
    ROOT / "src/data/environments/mixed_failures/flight_plans/flight_plan_ok.json",
    ROOT / "src/data/environments/mixed_failures/flight_plans/flight_plan2.json",
    ROOT / "src/data/environments/collision/flight_plans/flight_plan_opo_mad.json",
]

DATA_DIRS = [
    ROOT / "src/data/flight_plans",
    ROOT / "src/data/environments/all_valid/flight_plans",
    ROOT / "src/data/environments/out_of_fuel/flight_plans",
    ROOT / "src/data/environments/mixed_failures/flight_plans",
    ROOT / "src/data/environments/collision/flight_plans",
]


def leg_code(value):
    if isinstance(value, str):
        return value
    if isinstance(value, dict):
        return value.get("Airport", "")
    return ""


def harvest_embed_db():
    airports = {}
    aircraft = {}
    for path in REFERENCE_PLANS:
        if not path.exists():
            continue
        plan = json.loads(path.read_text())
        block = plan.get("Aircraft")
        if block and block.get("ModelId"):
            aircraft[block["ModelId"]] = block
        legs_key = "Legs" if "Legs" in plan else "Leg"
        for leg in plan.get(legs_key, []):
            for key in ("DepartureAirport", "ArrivalAirport"):
                block = leg.get(key)
                if block and block.get("Id"):
                    airports[block["Id"]] = block
    return airports, aircraft


def enrich_plan(plan, aircraft_db, airport_db):
    aircraft_id = plan.get("AircraftId") or plan.get("AircraftMaker")
    if aircraft_id and "Aircraft" not in plan:
        block = aircraft_db.get(aircraft_id)
        if block is None and aircraft_db:
            block = next(iter(aircraft_db.values()))
        if block:
            plan["Aircraft"] = block

    legs_key = "Legs" if "Legs" in plan else "Leg"
    for leg in plan.get(legs_key, []):
        dep = leg_code(leg.get("Departure"))
        arr = leg_code(leg.get("Arrival"))
        if dep and "DepartureAirport" not in leg and dep in airport_db:
            leg["DepartureAirport"] = airport_db[dep]
        if arr and "ArrivalAirport" not in leg and arr in airport_db:
            leg["ArrivalAirport"] = airport_db[arr]
    return plan


def main():
    aircraft_db, airport_db = harvest_embed_db()
    if not aircraft_db or not airport_db:
        raise SystemExit("No reference embedded data found — check REFERENCE_PLANS.")

    updated = 0
    for data_dir in DATA_DIRS:
        if not data_dir.exists():
            continue
        for path in sorted(data_dir.glob("*.json")):
            plan = json.loads(path.read_text())
            before = path.read_text()
            enrich_plan(plan, aircraft_db, airport_db)
            text = json.dumps(plan, indent=2) + "\n"
            if text != before:
                path.write_text(text)
                updated += 1
                print(f"updated {path}")
    print(f"done: {updated} files")


if __name__ == "__main__":
    main()
