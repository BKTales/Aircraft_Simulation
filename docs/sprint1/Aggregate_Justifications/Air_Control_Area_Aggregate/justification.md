### **AirControlArea Aggregate**

The `AirControlArea` aggregate is responsible for managing the identity and
geographic scope of an air control area within the AISafe system. It acts as
the **Aggregate Root**, ensuring that all data related to a specific area,
its code, geographic boundaries, and minimum fuel requirement is consistent
and valid.

#### **1. Aggregate Components**

* **AirControlArea (Aggregate Root):** The main entity that holds the area's
identity and owns its operational rules.
* **AreaCode (Value Object):** A system-generated positive integer that
uniquely identifies the air control area. Generated internally by the system
rather than provided by the user, ensuring controlled and consistent
assignment.
* **GeographicBoundary (Value Object):** A polygon defined by at least 3
`GeoCoordinates` points. Each coordinate must be within valid geographic
ranges (lat ∈ [-90, 90], lon ∈ [-180, 180]). Overlapping boundaries between
different areas are permitted by the domain.
* **MinFuelRequirement (Value Object):** A positive number representing the
minimum fuel quantity (in litres) that an aircraft must carry upon arriving
at an airport within this area, as per §3.1.4 of the requirements.

---

#### **2. Core Business Rules (Invariants)**

1. **AreaCode Uniqueness:** Although the `AreaCode` is system-generated, a
collision check is performed before creation to guard against race conditions.
If a collision is detected, an `AreaCodeCollisionException` is thrown and the
registration is aborted.
2. **GeographicalBoundary Validity:** A boundary must be composed of at least
3 `GeoCoordinates` to form a valid polygon. Each coordinate must respect
standard geographic bounds. These rules are enforced inside the
`GeographicalBoundary` Value Object at construction time. Boundaries are
permitted to overlap with other areas.
3. **MinFuelRequirement Positivity:** The minimum fuel requirement must be a
strictly positive number. This is enforced inside the `MinFuelRequirement`
Value Object at construction time.

---

#### **3. Aggregate Justification**

The **AirControlArea Aggregate** was designed around the principle that the
area is the natural guardian of its own geographic scope and operational
constraints.

The decision to model `GeographicBoundary` and `MinFuelRequirement` as
**Value Objects** rather than simple primitives reflects their domain
significance. `GeographicBoundary` is not merely a list of coordinates it
carries the invariant that a valid polygon must exist. `MinFuelRequirement`
is not merely a number — it carries the rule that fuel requirements must be
strictly positive, and has direct impact on flight plan validation (a flight
plan's `FuelLoad` must satisfy the `MinFuelRequirement` of the destination
area's `AirControlArea`). Both concepts have behaviour and validation rules
attached, which justifies elevating them to first-class domain objects.

The `AreaCode` is also a Value Object despite being system-generated. This
design choice ensures that the format rule is enforced at construction and
never violated by any part of the system. The uniqueness check, however, is a
cross-instance invariant and cannot be enforced inside the aggregate itself;
it is therefore the responsibility of the `AirControlAreaService`, consistent
with DDD principles.

The **geographical boundaries are permitted to overlap** between different
areas. This is an explicit domain decision aligned with the requirements: the
system does not model exclusive airspace ownership, and therefore the
`AirControlArea` aggregate has no obligation to verify whether its boundary
conflicts with others.
