### **WeatherData Aggregate**

The `WeatherData` aggregate is responsible for managing atmospheric
observations associated with a specific air control area within the AISafe
system. It acts as the **Aggregate Root**, ensuring that all meteorological
data: wind, temperature, pressure and humidity; is consistent, valid, and
properly associated with an area.

#### **1. Aggregate Components**

* **WeatherData (Aggregate Root):** The main entity that holds all
meteorological measurements for a given observation, associated with an
`AirControlArea` by ID only.
* **WindData (Value Object):** The wind conditions at the time of observation,
composed of direction (in degrees, ∈ [0, 360[, relative to North) and speed
(in m/s), as per §3.2 of the requirements. Both are enforced at construction.
* **Temperature (Value Object):** The atmospheric temperature at the time of
observation. Can be negative. Enforced at construction.
* **Pressure (Value Object):** The atmospheric pressure at the time of
observation. Must be a positive number. Used to compute air density and
thrust-at-altitude as per §3.3. Enforced at construction.
* **Humidity (Value Object):** The relative humidity at the time of
observation. Must be a percentage within [0, 100]. Enforced at construction.

---

#### **2. Core Business Rules (Invariants)**

1. **AirControlArea Association:** Weather data must be associated with an
existing `AirControlArea`. The existence of the area is verified before the
`WeatherData` is created. The reference is stored by ID only, no direct
object reference crosses aggregate boundaries.
2. **WindData Validity:** Direction must be within [0, 360[ degrees relative
to North. Speed must be a positive number in m/s. Enforced inside the
`WindData` Value Object at construction time.
3. **Pressure Positivity:** Atmospheric pressure must be a strictly positive
number. Enforced inside the `Pressure` Value Object at construction time.
4. **Humidity Range:** Relative humidity must be a percentage within [0, 100].
Enforced inside the `Humidity` Value Object at construction time.
5. **Temperature Validity:** Temperature must be a valid number and may be
negative. Enforced inside the `Temperature` Value Object at construction time.

---

#### **3. Aggregate Justification**

The **WeatherData Aggregate** was designed around the principle that a
meteorological observation is a cohesive, self-contained unit of data that
must be valid as a whole before being persisted.

All meteorological attributes, `WindData`, `Temperature`, `Pressure`, and
`Humidity`, are modelled as **Value Objects** because each carries its own
validation rule and domain significance. `WindData` in particular encapsulates
two related measurements (direction and speed) that only make sense together.
Separating them would lose the cohesion of the concept. `Pressure` has
additional domain significance: it is used directly in the physics
calculations described in §3.3 to derive air density and thrust-at-altitude,
making it a concept with behaviour attached rather than a plain number.

The association with `AirControlArea` is stored **by ID only**, consistent
with DDD aggregate boundary rules and reflected in the domain model. Weather
observations are area-scoped: a pilot or flight control operator consulting
weather data does so in the context of a specific area (US043), and a flight
references weather data associated with the relevant area. The existence of
the area is verified at creation time by the `WeatherDataService`, but the
aggregate itself only holds the `AreaCode` as a reference.

The `WeatherData` aggregate also acts as the bridge between the atmospheric
environment and the flight domain: a `Flight` may reference `WeatherData`
(US082), and the simulation engine uses it to incorporate environmental
factors such as wind into flight path calculations (US110). This makes
`WeatherData` a first-class domain concept rather than a simple data record.

---

#### **4. Design Note: US082**

As `FlightPlan` is a **Value Object** within the `Flight` aggregate, it has
no identity of its own and cannot be referenced directly by ID. To associate
weather data with a flight plan, the system navigates first to the `Flight`
via its `FlightDesignator`, and accesses the `FlightPlan` through the
aggregate itself (`flight.flightPlan.airControlAreaId`). This approach
respects aggregate boundaries. The `FlightPlan` is never accessed directly,
only through its natural owner, the `Flight` aggregate.

Unlike the other sequence diagrams in this project, **no Value Objects are
constructed in this flow**. This is intentional and correct: US082 is an
**association** use case, not a **creation** use case. Both the `Flight` and
the `WeatherData` already exist in the system. The `WeatherData` was created
and validated in US041 by the WeatherPerson, and the `Flight` was previously
registered. Constructing Value Objects here would be semantically incorrect,
as it would imply that new domain objects are being built, when in reality the
system is simply linking two already-valid, already-persisted aggregates.

The `WeatherData` referenced in this flow is guaranteed to be valid. Its
invariants (`WindData`, `Temperature`, `Pressure`, `Humidity`) were enforced
at construction time during US041. There is therefore no need to re-validate
it here. Doing so would violate the DDD principle that an object's validity
is established at construction and maintained by the aggregate, not re-checked
at every point of use.

Associating weather data with a flight is done via
`flight.assignWeatherData(weatherData)`. At persistence level, `Flight`
holds a JPA `@ManyToOne` reference to `WeatherData` (column `WEATHER_DATA_ID`
on table `FLIGHT`). The controller loads and validates the `WeatherData` entity
from the repository before passing it to the aggregate; the domain layer does not
resolve IDs on its own. Attaching weather does not change flight plan status.

The weather data displayed to the pilot is filtered by the `AirControlArea`
associated with the flight plan, accessed via
`flight.flightPlan.airControlAreaId`. This ensures that only meteorologically
relevant data is presented, consistent with the domain rule that weather
observations are area-scoped (US043).