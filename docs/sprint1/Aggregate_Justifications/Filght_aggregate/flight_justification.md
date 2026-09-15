## SD– US080: Create a Flight including its Flight Plan

### Overview

This sequence diagram illustrates the creation of a `Flight` together with its associated `FlightPlan`, as described in US080. The scenario is initiated by a **Pilot** and demonstrates the key invariant enforced by the **FlightAggregate**: a `FlightPlan` can only be created if the aircraft is active, the route is not deactivated, and the pilot belongs to the route's company.

### Flow Description

The Pilot interacts with the **FlightUI**, which delegates the request to the **FlightController**. The controller begins by loading the necessary domain objects from their respective repositories:

- The **Route** is retrieved to verify it is active and to determine the associated company.
- The **Aircraft** is retrieved to verify it is not decommissioned.
- The **Pilot** is retrieved to verify they belong to the route's company.

If all validations pass, the controller creates a new `Flight` entity. The `Flight` sets its **FlightSchedule** based on the departure date/time provided — using a specific date for charter flights, or days of the week for regular flights. Immediately after, the `Flight` creates its `FlightPlan` as a **Value Object**, which internally validates the following invariants:

- The aircraft is not decommissioned (US071).
- The route is not deactivated (US074).
- The pilot belongs to the route's company (US080).
- The fuel load is strictly positive.

The `FlightPlan` status is then set to **draft**, meaning it is registered but not yet validated or submitted for simulation.

Finally, the `Flight` (along with its embedded `FlightPlan`) is persisted via the **FlightRepository**, and a success response is returned to the Pilot.

### Key Design Decisions

| Decision | Justification |
|---|---|
| `FlightPlan` is a Value Object inside `Flight` | The flight plan has no independent lifecycle — it is always created and accessed through its parent `Flight`. |
| Departure date/time is set on `Flight`, not `FlightPlan` | Per the client's clarification, the schedule belongs to the flight instance. The plan uses this date when associating weather data (US082). |
| All invariant validation happens inside the domain objects | Following DDD best practices, the `Flight` and `FlightPlan` are responsible for enforcing their own rules, not the controller. |