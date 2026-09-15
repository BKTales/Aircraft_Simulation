### **Airport Aggregate**

The `Airport` aggregate is responsible for managing the identity, location,
and air control area association of an airport within the AISafe system. It
acts as the **Aggregate Root**, ensuring that all data related to a specific
airport: its codes, name, location, and area reference, is consistent and
valid.

#### **1. Aggregate Components**

* **Airport (Aggregate Root):** The main entity that holds the airport's
identity and references its associated air control area by ID only.
* **ICAOCode (Value Object):** The 4-letter uppercase ICAO airport code (e.g.,
"LPPT"). Must be unique worldwide. Format validation is enforced at
construction.
* **IATACode (Value Object):** The 3-letter uppercase IATA airport code (e.g.,
"LIS"). Format validation is enforced at construction.
* **AirportName (Value Object):** The human-readable name of the airport.
Enforced at construction.
* **AirportTown (Value Object):** The town where the airport is located.
Enforced at construction.
* **AirportCountry (Value Object):** The country where the airport is located.
Must be a valid, existing country. Enforced at construction.
* **GeoCoordinates (Value Object):** The geographic location and elevation of
the airport, composed of latitude, longitude, and altitude in meters above
sea level. Each value must be within valid ranges (lat ∈ [-90, 90],
lon ∈ [-180, 180]). Enforced at construction.

---

#### **2. Core Business Rules (Invariants)**

1. **ICAO Code Uniqueness:** No two airports can share the same `ICAOCode`.
This is a cross-instance invariant enforced by the `AirportService` via a
repository query before creation.
2. **ICAO Code Format:** `ICAOCode` must be exactly 4 uppercase letters.
Enforced inside the Value Object at construction time.
3. **IATA Code Format:** `IATACode` must be exactly 3 uppercase letters.
Enforced inside the Value Object at construction time.
4. **AirControlArea Association:** An airport must be associated with exactly
one existing `AirControlArea`. The existence of the area is verified before
the airport is created. The reference is stored by ID only, no direct object
reference crosses aggregate boundaries, consistent with the domain model.
5. **GeoCoordinates Validity:** Latitude must be within [-90, 90] and
longitude within [-180, 180]. Enforced inside the `GeoCoordinates` Value
Object at construction time.

---

#### **3. Aggregate Justification**

The **Airport Aggregate** was designed around the principle that the airport
is the natural guardian of its own identity codes and geographic location.

All location and identity attributes: `ICAOCode`, `IATACode`,
`GeoCoordinates`, `AirportName`, `AirportTown`, and `AirportCountry`  are
modelled as **Value Objects** because each carries its own format or validity
rule and has domain meaning beyond being a simple primitive. This ensures that
an `Airport` instance, once created, is always in a valid state  consistent
with the DDD principle of *invariant enforced at construction*.

The **ICAO uniqueness check** is a cross-instance invariant that cannot be
enforced inside the aggregate itself. It is therefore the responsibility of
the `AirportService`, which queries the repository before allowing creation.
This is consistent with DDD principles, where cross-aggregate rules are
handled at the application service level.

The association with `AirControlArea` is stored **by ID only**, with no direct
object reference crossing aggregate boundaries. This is a deliberate design
decision aligned with DDD aggregate boundary rules and reflected in the domain
model: the `Airport` aggregate does not own the `AirControlArea` and should
not hold a direct reference to it. The existence of the area is verified at
creation time by the service, but after that, the airport simply carries the
`AreaCode` as a reference.
